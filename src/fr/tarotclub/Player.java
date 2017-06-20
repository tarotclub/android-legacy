/*=============================================================================
 * TarotClub - Player.java
 *=============================================================================
 * Model of a player
 *=============================================================================
 * TarotClub ( http://www.tarotclub.fr ) - This file is part of TarotClub
 * Copyright (C) 2003-2999 - Anthony Rabine
 * anthony@tarotclub.fr
 *
 * This file must be used under the terms of the CeCILL.
 * This source file is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at
 * http://www.cecill.info/licences/Licence_CeCILL_V2-en.txt
 *
 *=============================================================================
 */

package fr.tarotclub;


public class Player {
  private Stats stats = new Stats(); 
  // public because too much pain if private
  public Deck mDeck = new Deck();

  /*****************************************************************************/
  public Player() {

  }
  /*****************************************************************************/
  /**
   * Play the first valid card in hands
   * Return the Id of the played card
   */
  public int play(Deck stack, Card[] cards, int gameCounter) {
     Card c = null;
     int i;
     
     for( i=0; i<mDeck.size(); i++ ) {
        c = cards[mDeck.getCard(i)];
        if( canPlayCard( stack, cards, c, gameCounter) == true ) {
        	break;
        }
     }
     if (c != null)
    	 return c.getId();
     else
    	 return -1;
  }
  /*****************************************************************************/
  /**
   * Update statistics by analyzing the deck
   */
  void updateStats(Card[] cards){
     int i, k, val, count, flag, longue, coul;
     Card c;

     stats.raz();
     count = 0; // compteur
     flag = 0;  // indicateur de petit

     // recherche des atouts
     for( i=0; i<mDeck.size(); i++) {
    	c = cards[mDeck.getCard(i)];
        if( c.getSuit() == Card.ATOUT ) {
           stats.atouts++;
           val = c.getValue();
           if( val >= 15 ) {
              stats.atoutsMajeurs++; // atout majeur
           }
           if( val == 21 ) {
              stats.vingtEtUn = true;
              stats.bouts++;
           }
           if( val == 1 ) {
              stats.petit = true;
              stats.bouts++;
           }
        }

        if( c.getSuit() == Card.EXCUSE ) {
           stats.atouts++;
           stats.bouts++;
           stats.excuse = true;
        }
     }

     int[] distr = new int[14]; // teste une distribution

     // on s'occupe maintenant des couleurs
     for( i=0; i<4; i++ ) {
        if( i==0 ) {
           coul = Card.SPADES;
        } else if( i==1 ) {
           coul = Card.HEARTS;
        } else if( i==2 ) {
           coul = Card.CLUBS;
        } else {
           coul = Card.DIAMONDS;
        }

        for( k=0; k<14; k++ ) {
           distr[k] = 0;
        }
        count = 0;

        for( k=0; k<mDeck.size(); k++ ) {
        	c = cards[mDeck.getCard(i)];
           if( c.getSuit() == coul ) {
              count++;
              val = c.getValue();
              distr[val-1] = 1;
              if( val == 11 ) {
                 stats.valets++; // valet
              }
              if( val == 12 ) {
                 stats.cavaliers++; // cavalier
              }
           }
        }

        if( count == 1 ) {
           stats.singletons++;     // singleton
        }
        if( count == 0 ) {
           stats.coupes++;         // coupe
        }

        // Nombre de cartes dans chaque couleur
        if( i==0 ) {
           stats.pics = count;
        } else if( i==1 ) {
           stats.coeurs = count;
        } else if( i==2 ) {
           stats.trefles = count;
        } else {
           stats.carreaux = count;
        }

        if( distr[13] == 1 && distr[12] == 1 ) {
           stats.mariages++; // mariage (dame+roi)
        }
        if( distr[13] == 1 ) {
           stats.rois++;     // roi sans dame
        }
        if( distr[12] == 1 ) {
           stats.dames++;    // dame sans roi
        }

        // test des s�quences :
        count = 0;  // longueur de la s�quence
        flag = 0;   // couleur trouv�e : on est dans la s�quence
        longue = 0;

        for( k=0; k<14; k++ ) {
           if( distr[k] == 1 ) {
              longue++;
              // d�but d'une s�quence
              if( flag == 0 ) {
                 flag = 1;
                 count++;
              } else {
                 count++;
              }
           } else if( flag == 1 ) {
              if( count >= 5 ) {
                 stats.sequences++;
              }
              count = 0;
              flag = 0;
           }
        }
        if( longue >=5 ) {
           stats.longues++;
        }
     }
  }
 
     /*****************************************************************************/
     /**
      * Bid calculation, return the bid of the player
      */
     int calculEnchere()
     {
        int total = 0;
        int cont;

        // On distribue des points en fonction des stats
        if( stats.vingtEtUn == true ) {
           total += 9;
        }
        if( stats.excuse == true ) {
           total += 7;
        }
        if( stats.petit == true ) {
           if( stats.atouts == 5 ) {
              total += 5;
           } else if( stats.atouts == 6 || stats.atouts == 7 ) {
              total += 7;
           } else if( stats.atouts > 7 ) {
              total += 8;
           }
        }

        // Chaque atout vaut deux points
        // Chaque atout majeur vaut 1 point suppl�mentaire
        total += stats.atouts * 2;
        total += stats.atoutsMajeurs * 2;
        total += stats.rois * 6;
        total += stats.dames * 3;
        total += stats.cavaliers * 2;
        total += stats.valets;
        total += stats.mariages;
        total += stats.longues * 5;
        total += stats.coupes * 5;
        total += stats.singletons * 3;
        total += stats.sequences * 4;

        // on d�cide de l'ench�re :
        if( total <= 35 ) {
           cont = Game.PASSE;
        } else if( total >= 36  && total <= 50 ) {
           cont = Game.PRISE;
        } else if( total >= 51  && total <= 65 ) {
           cont = Game.GARDE;
        } else if( total >= 66  && total <= 75 ) {
           cont = Game.GARDE_SANS;
        } else {
           cont = Game.GARDE_CONTRE;
        }
        return cont;
     }
     /*****************************************************************************/
     void choixChien(Deck chien, Card[] cards) {
        int i, j;
        Card c, cdeck;
        boolean ok=false;
        i=0;

        // on cherche si il y a un atout, une excuse, ou un roi dans le chien
        // et on les remplace par des cartes valides
        while( ok == false ) {
           c = cards[chien.getCard(i)];
           if ((	c.getSuit() == Card.ATOUT) || (c.getSuit() == Card.EXCUSE) ||
        		   ((c.getSuit() < Card.ATOUT) && (c.getSuit() == 14))) {
              // on cherche une carte de remplacement
              for( j=0; j<mDeck.size(); j++ ) {
                 cdeck = cards[mDeck.getCard(j)];
                 if ((cdeck.getSuit() < Card.ATOUT) && (cdeck.getValue() < 14)) {
                    // ok, on proc�de � l'�change
                	mDeck.removeCard(cdeck.getId());
                    mDeck.addCard(c.getId());
                    
                    chien.removeCard(c.getId());
                    chien.addCard(cdeck.getId());
                    break;
                 }
              }
              i=0;
           } else {
              i++;
           }
           if( i == 6 ) {
              ok = true;
           }
        }
     }
     /*****************************************************************************/
 	/**
 	 * Teste si la carte cVerif peut �tre jou�e : cela d�pend des cartes d�j�
 	 * jou�es sur le tapis et des cartes dans la main du joueur
 	 */
 	boolean canPlayCard(Deck stack, Card[] cards, Card cVerif , int gameCounter) {
 	   int debut;
 	   int rang;
 	   // premi�re carte (couleur demand�e)
 	   int type;
 	   
 	   debut = (gameCounter-1)/Game.NB_PLAYERS;
 	   debut = debut*Game.NB_PLAYERS;

 	   rang = gameCounter - debut;

 	   // Le joueur entame le tour, il peut commencer comme il veut
 	   if( rang == 1 ) {
 	      return true;
 	   }

 	   // on traite un cas simple
 	   if( cVerif.getSuit() == Card.EXCUSE ) {
 	      return true;
 	   }

 	   boolean possedeCouleur = false; // vrai si le joueur posse�de la couleur demandee
 	   boolean possedeAtout = false;   // vrai si le joueur poss�de un atout
 	   boolean precedentAtout = false; // vrai si un atout max pr�c�dent
 	   int  precAtoutMax = 0;
 	   int  atoutMax = 0; // atout maxi poss�d�
 	   boolean ret=true;
 	   Card c;
 	   int i,n;

 	   // on cherche la couleur demand�e
 	   c = cards[stack.getCard(debut)]; // premi�re carte jou�e � ce tour

 	   type = c.getSuit();
 	   if( type == Card.EXCUSE ) { // a�e, le joueur a commenc� avec une excuse
 	      // le joueur peut jouer n'importe quelle carte apr�s l'excuse, c'est lui qui d�cide alors de la couleur
 	      if( rang == 2 ) {
 	         return true;
 	      }
 	      c = cards[stack.getCard(debut+1)]; // la couleur demand�e est donc la seconde carte
 	      type = c.getSuit();
 	   }

 	   if((type < Card.ATOUT) && (type == cVerif.getSuit())) {
 	      return true;
 	   }

 	   // Quelques indications sur les cartes pr�c�dentes
 	   // On regarde s'il y a un atout
 	   for( i=0; i<rang-1; i++ ) {
 	      c = cards[stack.getCard(debut+i)];
 	      if( c.getSuit() == Card.ATOUT ) {
 	         precedentAtout = true;
 	         if( c.getValue() > precAtoutMax ) {
 	            precAtoutMax = c.getValue();
 	         }
 	      }
 	   }

 	   // Quelques indications sur les cartes du joueur
 	   n = mDeck.size();

 	   for( i=0; i<n; i++ ) {
 	      c = cards[mDeck.getCard(i)];

 	      if( c.getSuit() == Card.ATOUT ) {
 	         possedeAtout = true;
 	         if( c.getValue() > atoutMax ) {
 	            atoutMax = c.getValue();
 	         }
 	      }
 	      else if( c.getSuit() < Card.ATOUT ) {
 	         if( c.getSuit() == type ) {
 	            possedeCouleur = true;
 	         }
 	      }
 	   }

 	   // cas 1 : le type demand� est ATOUT
 	   if( type == Card.ATOUT ) {
 	      if( cVerif.getSuit() == Card.ATOUT ) {
 	         if( cVerif.getValue() > precAtoutMax ) {
 	            return true;
 	         } else {
 	            if( atoutMax > precAtoutMax ) {
 	               return false;  // doit surcouper !
 	            } else {
 	               return true;   // sinon tant pis, on doit quand m�me jouer la couleur demand�e
 	            }
 	         }
 	      } else {
 	         if( possedeAtout == true ) {
 	            return false;
 	         } else {
 	            return true;
 	         }
 	      }
 	   } else {// cas 2 : le type demand� est CARTE
 	      // le joueur poss�de la couleur demand�e, il doit donc la jouer cela
 	      if( possedeCouleur == true ) {
 	         return false;
 	      } else { // pas la couleur demand�e
 	         if( cVerif.getSuit() == Card.ATOUT ) {
 	            // doit-il surcouper ?
 	            if( precedentAtout==true ) {
 	               if( cVerif.getValue() > precAtoutMax ) { // carte de surcoupe
 	                  return true;
 	               } else {
 	                  // a-t-il alors un atout plus fort ?
 	                  if( atoutMax > precAtoutMax ) {
 	                     return false; // oui, il doit donc surcouper
 	                  } else {
 	                     return true;
 	                  }
 	               }
 	            }
 	         } else {
 	            // a-t-il un atout pour couper ?
 	            if( possedeAtout == true ) {
 	               return false; // oui, il DOIT couper
 	            } else {
 	               return true; // non, il peut se d�fausser
 	            }
 	         }
 	      }
 	   }
 	   return ret;
 	}
}

/*****************************************************************************/
class Stats {
		
   public int   atouts;  // nombres d'atouts , en comptant les bouts et l'excuse
   public int   bouts;   // 0, 1, 2 ou 3
   public int   atoutsMajeurs; // atouts >= 15

   public int   rois;
   public int   dames;
   public int   cavaliers;
   public int   valets;

   public int   mariages;   // nombre de mariages dans la main
   public int   longues;
   public int   coupes;     // aucune carte dans une couleur
   public int   singletons; // une seule carte dans une couleur
   public int   sequences;  // cartes qui se suivent (au moins 5 cartes pour �tre compt�es)
   public int   trefles;
   public int   pics;
   public int   coeurs;
   public int   carreaux;
   
   public boolean  petit;
   public boolean  vingtEtUn;
   public boolean  excuse;
   /*****************************************************************************/
   Stats() {
	   raz();
   }
   /*****************************************************************************/
   void raz() {
    atouts = 0;
	bouts = 0;
	atoutsMajeurs = 0;
	
	rois = 0;
	dames = 0;
	cavaliers = 0;
	valets = 0;
	
	mariages = 0;
	longues = 0;
	coupes = 0;
	singletons = 0;
	sequences = 0;
	
	trefles = 0;
	pics = 0;
	coeurs = 0;
	carreaux = 0;
	
	petit = false;
	vingtEtUn = false;
	excuse = false;	   
   }
   
   // End of class
}

//=============================================================================
//End of file Player.java
//=============================================================================
