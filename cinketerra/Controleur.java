/*
* Auteur : Équipe 1
* Date   : juin 2023
* */


/*      Paquetage      */
package cinketerra;


/*       Imports       */
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import cinketerra.ihm.*;
import cinketerra.metier.*;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.Toolkit;


/**
 * Controlleur de l'application, sert majoriterement de passerelle.
 */
public class Controleur implements WindowStateListener
{


	/* Attributs de Classe */
	private static int NB_JOUEUR = -1;


	/*      Attributs      */
	private static boolean option = false;

	private Mappe       metier1;
	private FrameGame   ihmMappe1;

	private Mappe       metier2;
	private FrameGame   ihmMappe2;

	private FrameCartes ihmPioche;


	/*    Constructeur     */
	public Controleur( boolean debug )
	{
		FrameDebut frameD = new FrameDebut(this, debug);

		while (Controleur.NB_JOUEUR == -1)
		{
			try { Thread.sleep(100); } catch (Exception e) {}
		}

		frameD.dispose();

		PaquetDeCarte p = new PaquetDeCarte();

		this.metier1    = new Mappe(this, p);
		this.ihmMappe1  = new FrameGame(this, 1);

		if (Controleur.NB_JOUEUR == 2)
		{
			this.metier2    = new Mappe    (this, p);
			this.ihmMappe2  = new FrameGame(this, 2);
		}

		this.ihmPioche = new FrameCartes(this);

		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		int hauteur = (int) (screen.getHeight()) - 3 * 45;
		int largeur = (int) (screen.getWidth ());

		if (Controleur.NB_JOUEUR == 2)
		{
			this.ihmMappe1 .setSize( (int) (largeur * 0.5) - 90, (int) ( 0.75 * hauteur ) );
			this.ihmMappe2 .setSize( (int) (largeur * 0.5) - 90, (int) ( 0.75 * hauteur ) );
			this.ihmMappe2 .setLocation( (int)(largeur * 0.5) + 45, 45 );
			this.ihmMappe2.addWindowStateListener(this);
		}
		else
		{
			this.ihmMappe1 .setSize( largeur  - 90, (int) ( 0.75 * hauteur ) );
		}

		this.ihmPioche.setSize( largeur - 60, (int) ( 0.30 * hauteur ) );
		
		this.ihmMappe1 .setLocation( 45, 45 );
		this.ihmPioche.setLocation( 30, 55 + this.ihmMappe1.getHeight() );

		this.ihmMappe1.addWindowStateListener(this);
		this.ihmPioche.addWindowStateListener(this);
	}




	/*     Accesseurs      */


	//Controleur
	public boolean getOptionActive() { return Controleur.option ;}
	

	//Mappe - Metier
	public List<Ile>    getIles(int id) 
	{
		if (id == 1) return this.metier1.getIles();
		else         return this.metier2.getIles();
	}

	public List<Chemin> getChemins(int id) 
	{
		if (id == 1) return this.metier1.getChemins();
		else         return this.metier2.getChemins();
	}

	public List<Region> getRegions(int id) 
	{
		if (id == 1) return this.metier1.getRegions();
		else         return this.metier2.getRegions();
	}

	public Color   getColFeutre(int id) 
	{ 
		if (id == 1) return this.metier1.getColFeutre();
		else         return this.metier2.getColFeutre();
	}

	public Ile getIleDebut(int id) 
	{
		if (id == 1) return this.metier1.getIleDebut();
		else         return this.metier2.getIleDebut();
	}

	public String getScore (int id)
	{
		if (id == 1) return this.metier1.getScore();
		else         return this.metier2.getScore();
	}


	//Cartes - Metier
	public List<Carte> getCartes() { return this.metier1.getCartes(); } //Le paquet est commun, on prend donc celui du joueur 1
	public boolean carteBonusEstActive(int id)
	{
		if (id == 1) return this.metier1.carteBonusEstActive();
		else         return this.metier2.carteBonusEstActive();
	}

	public boolean bonusAEteActive(int id)
	{
		if (id == 1) return this.metier1.bonusAEteActive();
		else         return this.metier2.bonusAEteActive();
	}


	//Mappe - IHM
	public int getHauteur(int id) 
	{
		if (id == 1) return this.ihmMappe1.getHeight();
		else         return this.ihmMappe2.getHeight();
	}

	public int getLargeur(int id) 
	{
		if (id == 1) return this.ihmMappe1.getWidth();
		else         return this.ihmMappe2.getWidth();
	}


	//Pioche - IHM
	public int getLargeurPioche() { return this.ihmPioche.getWidth() ; }
	public int getHauteurPioche() { return this.ihmPioche.getHeight(); }


	//Carte - Metier
	public Carte getCarte     (int indice) { return this.metier1.getCarte(indice); }

	public int   getNbCarteTotal        () { return this.metier1.getNbCarteTotal        (); }
	public Carte getDerniereCartePiochee() { return this.metier1.getDerniereCartePiochee(); }
	public int   getNbCarteRestante     () { return this.metier1.getNbCarteRestante     (); }

	public boolean carteCachee(int indice)
	{
		if (this.metier1.getCarte(indice) == null) return false;
		return this.metier1.getCarte(indice).estCache();
	}

	public String getImageBonus(int id)
	{
		String sRet = "./resources/cartes/bonus_" + Mappe.getCarteBonus().name().toLowerCase();

		if (id == 1 && this.metier1.bonusAEteActive())
			return sRet + "_dechire.png";

		if (id == 2 && this.metier2.bonusAEteActive())
			return sRet + "_dechire.png";

		if (id == 1 && this.metier1.carteBonusEstActive())
			return sRet + "_select.png";
			
		if (id == 2 && this.metier2.carteBonusEstActive())
			return sRet + "_select.png";

		return sRet + ".png";
	}

	public String getImage(int indice)
	{
		String sRet = "./resources/cartes/";
		Carte c = this.metier1.getCarte(indice);

		if (c != null && !c.estCache())
			sRet += (c.getContour().equals(Color.white) ? "blanc_" : "noir_") + c.getCouleur().toLowerCase() + ".png";
		else
			sRet += "carte_dos.png";

		return sRet;
	}

	public String getImageRetournee(Carte c)
	{
		String sRet = "./resources/cartes/";

		sRet += (c.getContour().equals(Color.white) ? "blanc_" : "noir_") + c.getCouleur().toLowerCase() + ".png";

		return sRet;
	}




	/*     Modifieurs      */
	public static void setOptionActive( boolean state ) { Controleur.option    = state ; }
	public static void setNbJoueur    ( int     nb    ) { Controleur.NB_JOUEUR = nb    ; }




	/*      Méthodes       */

	// Cartes - Metier
	public void activerCarteBonus(int id)
	{
		if (id == 1) this.metier1.activerCarteBonus();
		else         this.metier2.activerCarteBonus();
	}

	public void piocher( int indice )
	{
		this.metier1.piocher( indice );

		if (Controleur.NB_JOUEUR == 2)
		{
			this.metier2.piocher();
			this.ihmMappe2.maj();
		}

		if (this.getNbCarteTotal() - this.getNbCarteRestante() == Mappe.getTourEvent("Bifurcation"))
		{
			JFrame f = new FrameAnnonce( "zeppelin" );
			Mappe.addAction(new Mouvement("Bifurcation en cours...", "bifurcation"));
		}

		this.majIHM();
	}


	//Mappe - Metier
	public Chemin  trouverChemin (Ile i1, Ile i2, int id) 
	{
		if (id == 1) return this.metier1.trouverChemin(i1,i2);
		else         return this.metier2.trouverChemin(i1,i2);
	}

	public boolean colorier (Chemin c, int id) 
	{ 
		if (id == 1) return this.metier1.colorier(c, id);
		else         return this.metier2.colorier(c, id);
	}

	public boolean estColoriable(Chemin c, int id) 
	{ 
		if (id == 1) return this.metier1.estColoriable(c);
		else         return this.metier2.estColoriable(c);
	}


	// IHM
	public void majIHM ()
	{
		if (this.ihmPioche != null) this.ihmPioche.maj();
		if (this.ihmMappe1 != null) this.ihmMappe1.maj();
		if (this.ihmMappe2 != null) this.ihmMappe2.maj();
	}
	
	public void windowStateChanged(WindowEvent e)
	{
		if (JFrame.class.cast(e.getSource()).getState() == JFrame.ICONIFIED) {
			this.ihmMappe1.setState(JFrame.ICONIFIED);
			if (this.ihmMappe2 != null)
				this.ihmMappe2.setState(JFrame.ICONIFIED);
			this.ihmPioche.setState(JFrame.ICONIFIED);
		}

		if (JFrame.class.cast(e.getSource()).getState() == JFrame.NORMAL) {
			this.ihmMappe1.setState(JFrame.NORMAL);
			if (this.ihmMappe2 != null)
				this.ihmMappe2.setState(JFrame.NORMAL);
			this.ihmPioche.setState(JFrame.NORMAL);
		}
	}


	// Controleur
	public void initialiserManche ()
	{
		this.metier1.initialiserManche();
		if (this.metier2 != null) this.metier2.initialiserManche();

		this.ihmPioche.hideButton();
		this.ihmPioche.initPioche();
		this.ihmMappe1.newManche();
		if (this.ihmMappe2 != null) this.ihmMappe2.newManche();

	}


	// IHM - Pioche
	public void showButton() {this.ihmPioche.showButton();}
	public void bloquerPioche(boolean bloque) { if (this.ihmPioche != null) this.ihmPioche.bloquerPioche(bloque); }

	public void finDePartie ()
	{
		String mess;
		Color c1 = this.getColFeutre(1);

		String[] tabScore1 = this.metier1.getScore().split("; ");
		String[] tabScore2 = new String[1];
		
		// Score du joueur 1
		
		String rgb = "rgb(" + c1.getRed() + ", " + c1.getGreen() + ", " + c1.getBlue() + ")";
		
		mess  = "<html>Point du <font style='color: " + rgb + "'>Joueur 1 : " + tabScore1[0] + " points</font><br><ul>";
		mess += "<font style='color: " + rgb + "'> " + tabScore1[1].substring(15) + " points</font> (" + tabScore1[1].substring(0,13) + ")<br>";
		mess += "<font style='color: " + rgb + "'> " + tabScore1[2].substring(12) + " points</font> (" + tabScore1[2].substring(0,10) + ")</ul>";

		if (Controleur.NB_JOUEUR == 2)
		{
			Color c2 = this.getColFeutre(2);
			// Score du joueur 2
			tabScore2 = this.metier2.getScore().split("; ");

			mess += "<br>Point du <font style='color: rgb(" + c2.getRed() + ", " + c2.getGreen() + ", " + c2.getBlue() + ")'>Joueur 2 : " + tabScore2[0] + " points</font> <br><ul>";
			mess +=   "<font style='color: rgb(" + c2.getRed() + ", " + c2.getGreen() + ", " + c2.getBlue() + ")'> " + tabScore2[1].substring(15) + " points</font> (" + tabScore2[1].substring(0,13) + ")<br>";
			mess +=   "<font style='color: rgb(" + c2.getRed() + ", " + c2.getGreen() + ", " + c2.getBlue() + ")'> " + tabScore2[2].substring(12) + " points</font> (" + tabScore2[2].substring(0,10) + ")</ul>";
		}

		JOptionPane.showConfirmDialog( null, mess+"</html>" , "Fin de la partie", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
		
		if (Controleur.NB_JOUEUR == 1)
			this.metier1.enregistrerMouvement(Integer.parseInt(tabScore1[0]), null);
		else
			this.metier1.enregistrerMouvement(Integer.parseInt(tabScore1[0]), Integer.parseInt(tabScore2[0]));

		if (this.ihmMappe2 != null) this.ihmMappe2.dispose();

		this.ihmMappe1.dispose();
		this.ihmPioche.dispose();
	}


	// IHM - Historique
	public void hideHistorique (int id) 
	{
		if (id == 1) this.ihmMappe1.hideHistorique();
		else         this.ihmMappe2.hideHistorique();
	}
	public void showHistorique (int id) 
	{
		if (id == 1) this.ihmMappe1.showHistorique();
		else         this.ihmMappe2.showHistorique();
	}

	/*      Scénario       */

	// Méthode de forçage de la pioche
	public void forcePioche( Carte c )
	{
		int indice = 0;

		for (Carte carte : this.getCartes())
		{
			if ( carte.estCache()      ) indice++;
			if ( c    .equals  (carte) ) break;
		}

		this.ihmPioche.forcePioche( indice );
	}

	public void melangerPioche( boolean etat )
	{
		this.metier1.melangerPioche(etat);
	}

	// Force Colorier
	public void forceColorier(int joueur, Chemin chemin, Color color)
	{
		if ( joueur == 1) this.metier1.colorier(chemin, joueur);
		if ( joueur == 2) this.metier2.colorier(chemin, joueur);
	}

	//Méthode de forçage de la bifurcation
	// public void setTourEventBifurcation( int tour )
	// {
	//  	Mappe.setTourEventBifurcation( tour );
	// }

	// Méthode de forçage de la carte bonus
	// public void forceCarteBonus(CarteBonus cb)
	// {
	// 	Mappe.forceCarteBonus(cb);
	// }

	public void lancerScenario(int num )
	{
		this.metier1.lancerScenario( num );
	}


	// Main
	public static void main(String[] args)
	{
		new Controleur( args.length >= 1 && args[0].equals("debug") );
	}
}