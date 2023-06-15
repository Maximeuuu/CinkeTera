package debug.scenario.metier;


/** Lecture */
import java.util.Scanner;

import debug.scenario.Controleur;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/** Listes */
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
/** AWT */
import java.awt.Color;
import java.awt.geom.Line2D;

/**
 * Représentation de la carte de l'archipel.
 */
public class Mappe 
{
	/*----------------------------------*/
	/*           CONSTANTES             */
	/*----------------------------------*/

	/** Chemin relatif du fichier de données */
	private static final String      NOM_FICHIER = "./resources/";

	/** Liste de constantes de couleurs */
	private static List<Color>       colors;

	private static List<Mouvement>   lstHistorique;

	private static long              debutPartieTemps;

	private static List<CarteBonus>  lstCarteBonus;

	/** Evenements */
	private static int    tourEventBifurcation;

	/*----------------------------------*/
	/*           ATTRIBUTS              */
	/*----------------------------------*/

	private Controleur ctrl;

	/** Liste de toutes les îles */
	private List<Ile>     lstIles;

	/** Liste de toutes les régions */
	private List<Region>  lstRegions;

	/** List de tout les chemins */
	private List<Chemin>  lstChemins;

	/** Île de départ */
	private Ile           ileDeDepart;
	private boolean       mancheTermine;

	/** Liste des chemins coloriés */
	private List<Chemin>  lstCheminColorie;
	private boolean       estDebutManche;

	/** Paquet de cartes */
	private PaquetDeCarte paquet;
	private boolean       aJouer;

	/** Carte bonus de la manche */
	private static CarteBonus    carteBonus;
	private boolean              carteBonusActive;
	private boolean              bonusAEteActive;

	/** Bonus BIS */
	private String               colBonus;
	private boolean              bonusBis;

	private Region               regionBonus;

	/** Couleur du feutre */
	private Color         feutre;

	/** Le nombre de points */
	private String        points;

	/** Tests & Forcing */
	private boolean piocheMelangee = true;

	/**
	 * Constructeur sans paramètres qui initialise l'objet.
	 */
	public Mappe(Controleur ctrl, PaquetDeCarte p) 
	{
		this.ctrl   = ctrl;
		this.paquet = p;
		
		this.points = "0; bonus chemins: 0; bonus îles: 0";

		this.regionBonus = null;

		this.initialise();
	}

	/**
	 * Initialise et lit le fichier csv.
	 */
	public void initialise()
	{
		Mappe.debutPartieTemps = System.currentTimeMillis();
		Mappe.lstHistorique    = new ArrayList<>();
		Mappe.lstCarteBonus    = new ArrayList<>(Arrays.asList(CarteBonus.values()));

		// Mélange de la liste des cartes bonus
		Collections.shuffle(Mappe.lstCarteBonus);
		
		if (Mappe.colors == null)
			if ((int) (Math.random()*2) == 1) Mappe.colors = new ArrayList<>(Arrays.asList( Color.RED , Color.BLUE));
			else                              Mappe.colors = new ArrayList<>(Arrays.asList( Color.BLUE, Color.RED ));
		else
			if (Mappe.colors.get(0) == Color.RED) Collections.addAll(Mappe.colors, Color.RED , Color.BLUE);
			else                                  Collections.addAll(Mappe.colors, Color.BLUE, Color.RED );



		this.lstRegions       = new ArrayList<>();
		this.lstIles          = new ArrayList<>();
		this.lstChemins       = new ArrayList<>();
		this.lstCheminColorie = new ArrayList<>();
		
		try
		{
			Scanner scan = new Scanner(new FileInputStream(Mappe.NOM_FICHIER + "data.data"), StandardCharsets.UTF_8);

			while (scan.hasNextLine()) {
				String s = scan.nextLine();
				String[] ensInfo = s.split("\t");

				// Si il y a juste le nom de la région
				if (ensInfo.length == 1 && !ensInfo[0].equals(""))
					this.lstRegions.add(new Region(ensInfo[0]));

				// Si il y a les infos d'une île
				if (ensInfo.length == 9)
					this.lstIles.add(new Ile(this.lstRegions.get(this.lstRegions.size() - 1), // Dernière region
							ensInfo[0], ensInfo[1], // Nom et Couleur
							Integer.parseInt(ensInfo[2]), Integer.parseInt(ensInfo[3]),   // Coords point
							Integer.parseInt(ensInfo[5]), Integer.parseInt(ensInfo[6]),   // Coords image
							Integer.parseInt(ensInfo[7]), Integer.parseInt(ensInfo[8]))); // Coords nom

				// Si il y a les infos pour un chemin
				if (ensInfo.length == 2)
					this.lstChemins.add(new Chemin(this.getIleId(ensInfo[0]), this.getIleId(ensInfo[1]), 0));

				// Si il y a les infos pour un chemin bonus
				if (ensInfo.length == 3)
					this.lstChemins.add(new Chemin(this.getIleId(ensInfo[0]), this.getIleId(ensInfo[1]),
							Integer.parseInt(ensInfo[2])));

			}
		}
		catch (Exception e)
		{
			System.out.println("Nom fichier invalide : " + Mappe.NOM_FICHIER);
		}

		this.initialiserManche();
	}

	/**
	 * Initialise la manche et définit la couleur du feutre.
	 */
	public void initialiserManche()
	{
		// S'il n'y a plus de couleurs disponibles
		if (Mappe.colors.size() == 0)
		{
			this.ctrl.finDePartie();
			Mappe.colors.add(Color.WHITE);
			return;
		}
		
		// S'il reste des couleurs disponibles
		this.paquet.reinitialiser();

		if (this.piocheMelangee) this.paquet.melanger();

		this.ctrl.bloquerPioche(false);

		this.feutre = Mappe.colors.remove(0);

		Mappe.carteBonus      = Mappe.lstCarteBonus.remove(0);
		this.carteBonusActive = false;
		this.bonusAEteActive  = false;

		if (this.feutre.equals(Color.RED))
			this.ileDeDepart = this.getIleId("Ticó");
		else
			this.ileDeDepart = this.getIleId("Mutaa");

		this.estDebutManche = true;

		Mappe.tourEventBifurcation = (int) ( Math.random() * 11 );

		System.out.println("Nouvelle manche avec coul :" + this.feutre);

		this.ctrl.majIHM();

		this.mancheTermine = true;
	}
	
	public Chemin trouverChemin (Ile i1, Ile i2)
	{
		for (Chemin c1 : i1.getCheminAutour())
			for (Chemin c2 : i2.getCheminAutour())
				if (c1 == c2)
					return c1;

		return null;
	}

	public static int getTourEvent(String event)
	{
		if (event.equals("Bifurcation")) 
		{
			return Mappe.tourEventBifurcation;
		}

		return 0;
	}

	/**
	 * Retourne la liste des îles.
	 * 
	 * @return la liste des îles de la mappe
	 */
	public List<Ile> getIles() { return this.lstIles; }
	
	/**
	 * Retourne la liste des chemins.
	 * 
	 * @return la liste des chemins de la mappe
	 */
	public List<Chemin> getChemins() { return this.lstChemins; }

	public List<Region> getRegions() { return this.lstRegions; }

	/**
	 * Retourne un objet {@code Ile} associé à un objet {@code String}.
	 * 
	 * @param nom - le nom de l'île souhaitée
	 * @return une {@code Ile} en fonction du nom, {@code null} si inexistante
	 */
	public Ile getIleId(String nom) 
	{
		return this.lstIles.stream()
		       .filter(i -> i.getNom().equals(nom))
		       .findFirst()
		       .orElse(null);
	}

	/**
	 * Retourne une {@code Carte} en fonction de son indice.
	 * 
	 * @param indice - l'indice associé à une carte
	 * @return une {@code Carte}
	 */
	public Carte   getCarte(int indice) { return this.paquet.getCarte(indice); }

	public static  CarteBonus getCarteBonus() {return Mappe.carteBonus;}

	public void    activerCarteBonus()   { this.carteBonusActive = !this.carteBonusActive; }

	public boolean carteBonusEstActive() { return this.carteBonusActive;                   }

	public boolean bonusAEteActive()     { return this.bonusAEteActive;                    }

	/**
	 * Retourne le nombre total de cartes.
	 * 
	 * @return le nombre de cartes
	 */
	public int getNbCarteTotal   () { return this.paquet.getNbCarteTotal();    }

	/**
	 * Retourne le nombre total de cartes non-piochées.
	 * 
	 * @return le nombre de cartes nonpiochées
	 */
	public int getNbCarteRestante() { return this.paquet.getNbCarteRestante(); }

	/**
	 * Retourne le nombre total de cartes.
	 * 
	 * @return l'île du début
	 */
	public Ile getIleDebut() { return this.ileDeDepart; }

	/**
	 * Retourne une {@code List} de {@code Carte} de toutes
	 * les cartes du paquet.
	 * 
	 * @return une liste de cartes
	 */
	public List<Carte> getCartes()
	{
		return this.paquet.getCartes();
	}

	public Carte getDerniereCartePiochee()
	{
		return this.paquet.getDerniereCartePiochee();
	}

	/**
	 * Pioche une carte parmi le paquet à un indice voulu. 
	 * 
	 * @param indice - l'indice de la carte à piocher
	 */
	public void piocher(int indice)
	{
		if (this.paquet.getNbNoiresPiochees() != 5)
		{
			this.paquet.piocher(indice);
		}

		this.piocher();

		if (this.paquet.getNbCarteRestante() == 0)
			this.ctrl.showButton();
	}
	
	public void piocher()
	{
		if (this.paquet.getNbNoiresPiochees() == 5)
		{
			this.ctrl.bloquerPioche(true);
			this.ctrl.showButton();
		}

		this.aJouer = false;
	}



	/*----------------------------------*/
	/*           COLORIAGE              */
	/*----------------------------------*/

	/**
	 * Colorie un {@code Chemin}, seulement si il est coloriable.
	 * 
	 * @param c - le chemin à colorier
	 * @return {@code vrai} si il a été colorié, sinon {@code faux}
	 * @see {@link Mappe#estColoriable(Chemin)}
	 */
	public boolean colorier(Chemin c, int id) 
	{
		if (!this.estColoriable(c))
			return false;

		if (c.getCouleurPrim() == null)
			c.setCouleurPrim(this.feutre);
		else
			c.setCouleurSec(this.feutre);
			
		if (this.bonusBis) this.bonusBis = false;

		if (this.carteBonusActive && !this.bonusAEteActive && Mappe.carteBonus.ordinal() == 0)
		{
			if (!this.ileAppartientALigne(c.getIleA()))
				this.colBonus = c.getIleA().getCoul();

			if (!this.ileAppartientALigne(c.getIleB()))
				this.colBonus = c.getIleB().getCoul();

			System.out.println("L'ile " + c.getIleA().getCoul() + "appartient ? " + this.ileAppartientALigne(c.getIleA()));
			System.out.println("L'ile " + c.getIleB().getCoul() + "appartient ? " + this.ileAppartientALigne(c.getIleB()));

			System.out.println("colBonus " + this.colBonus);

			this.bonusBis = true;
			//(this.carteBonusActive && !this.bonusAEteActive && Mappe.carteBonus.ordinal() == 0)
		}

		this.lstCheminColorie.add(c);


		this.estDebutManche = false;
		
		this.aJouer = true ;
		

		Mappe.lstHistorique.add(new Mouvement(id, c));

		if (this.carteBonusActive && Mappe.carteBonus.ordinal() == 3)
		{
			if (c.getIleA().getNbCheminsColorie(this.feutre) == 1)
				this.regionBonus = c.getIleA().getReg();
			else
				this.regionBonus = c.getIleB().getReg();
		}


		if (!this.bonusAEteActive)
			this.bonusAEteActive = this.carteBonusActive;

		this.carteBonusActive = false;

		this.recalculerPoints();
		this.ctrl.majIHM();

		return true;
	}

	/**
	 * Indique si un {@code Chemin} est coloriable ou non.
	 * <br><br>
	 * Pour qu'un chemin soit coloriable, il doit respecter ces conditions :
	 * <ul>
	 *   <li>Il ne doit pas être {@code null} ;</li>
	 *   <li>Il ne doit pas déjà être colorié ;</li>
	 *   <li>Il ne doit pas croiser une arête déjà coloriée ;</li>
	 *   <li>Il ne doit pas former un cycle ;</li>
	 *   <li>Il ne doit pas avoir plus d'un seul chemin séléctionné et de la même couleur autour de lui ;</li>
	 * </ul>
	 * <br><br>
	 * @param c - le chemin à tester
	 * @return {@code vrai} si le chemin est coloriable, autrement {@code faux}
	 * @see {@link Mappe#colorier(Chemin)}
	 */
	public boolean estColoriable(Chemin c) 
	{
		if (c == null) return false;
		/* Si le chemin n'existe pas ou que on peut pas jouer de carte */
		if ((this.aJouer) ^ this.bonusBis) return false;

		/* Si le chemin est déjà colorié */
		if (c.getCouleurPrim() != null && !(this.carteBonusActive && !this.bonusAEteActive && Mappe.carteBonus.ordinal() == 2)) 
			return false;

		Ile ileA = c.getIleA();
		Ile ileB = c.getIleB();

		/* Si le bonus BIS est actif */
		if (this.bonusBis)
		{
			System.out.println("ileA coul = " + ileA.getCoul() + " ileB coul = " + ileB.getCoul());
			System.out.println("ileA appartient " + this.ileAppartientALigne(ileA) + "  -  ileB appartient " + this.ileAppartientALigne(ileB));

			if (!((!this.ileAppartientALigne(ileA) && ileA.getCoul().equals(this.colBonus)) || (!this.ileAppartientALigne(ileB) && ileB.getCoul().equals(this.colBonus)))) return false;
		}

		/* Dans le cas où il s'agit du premier trait */

		/* Si le chemin croise une arête déjà coloriée */
		if (this.cheminCroise(c) && !(this.carteBonusActive && !this.bonusAEteActive && Mappe.carteBonus.ordinal() == 1))
			return false;

		/* Si le chemin forme un cycle */
		if (this.aCycle(c)) return false;

		if (this.estDebutManche)
			if (ileA == this.ileDeDepart && this.bonneCouleur(ileB) ||
			    ileB == this.ileDeDepart && this.bonneCouleur(ileA)) //Bonne ile : Okay
				return true;
			else
				return false;

		/* Si c'est une extrémité ou si la direction est pas une bonne couleur */
		if (this.getNbCarteTotal() - this.getNbCarteRestante() == Mappe.getTourEvent("Bifurcation"))
			return (this.ileAppartientALigne(ileB) && this.bonneCouleur(ileA)) || (this.ileAppartientALigne(ileA) && this.bonneCouleur(ileB));
		else
			return (this.cheminsColorieAutour(ileB) && this.bonneCouleur(ileA)) || (this.cheminsColorieAutour(ileA) && this.bonneCouleur(ileB));

		//return false;
	}

	private boolean cheminsColorieAutour (Ile i)
	{
		int nbColorie = 0;

		for (Chemin chemin : i.getCheminAutour())
			if (chemin.getCouleurPrim() == this.feutre || chemin.getCouleurSec() == this.feutre)
				nbColorie ++;

		return nbColorie == 1;
	}

	private boolean ileAppartientALigne(Ile i)
	{
		for (Chemin chemin : i.getCheminAutour())
			if (this.lstCheminColorie.contains(chemin)) return true;

		return i.equals(this.ileDeDepart);
	}

	private boolean aCycle(Chemin a1)
	{ 
	
		for (Chemin a : a1.getIleA().getCheminAutour())
			if ( this.feutre.equals(a.getCouleurPrim()) )
				for (Chemin a2 : a1.getIleB().getCheminAutour())
					if ( this.feutre.equals(a2.getCouleurPrim()) ) return true;

		return false;
	}

	private boolean cheminCroise(Chemin c1) 
	{
		int x1 = c1.getIleA().getXPoint();
		int y1 = c1.getIleA().getYPoint();
		int x2 = c1.getIleB().getXPoint();
		int y2 = c1.getIleB().getYPoint();

		for (Chemin c2 : this.lstChemins) 
		{
			if (c2.getCouleurPrim() != null && !c1.ileIdentique(c2)) 
			{
				int x3 = c2.getIleA().getXPoint();
				int y3 = c2.getIleA().getYPoint();
				int x4 = c2.getIleB().getXPoint();
				int y4 = c2.getIleB().getYPoint();

				if (Line2D.linesIntersect(x1, y1, x2, y2, x3, y3, x4, y4))
					return true;
			}

		}

		return false;
	}
	

	private boolean bonneCouleur(Ile i)
	{
		if (this.paquet.getDerniereCartePiochee() == null) return false;

		String coul = this.paquet.getDerniereCartePiochee().getCouleur();

		if (coul == null) return false;

		return coul.equals("Multi") || coul.equals(i.getCoul());
	}

	/**
	 * Retourne la couleur du stylo.
	 * 
	 * @return la couleur du stylo
	 */
	public Color getColFeutre() { return this.feutre; }

	public void recalculerPoints()
	{
		List<Region> lstRegionsParcourues = new ArrayList<>();
		List<Ile>    lstIlesParcourues    = new ArrayList<>();

		int tempIles = 0;
		int nbMaxIles = 0;

		for (Chemin c : this.lstCheminColorie)
		{
			Ile ile = c.getIleA();
			
			lstRegionsParcourues.add(ile.getReg());
			lstIlesParcourues.add(ile);

			ile = c.getIleB();
			
			lstRegionsParcourues.add(ile.getReg());
			lstIlesParcourues.add(ile);
		}

		lstIlesParcourues = lstIlesParcourues.stream().distinct().toList();
		lstRegionsParcourues = lstRegionsParcourues.stream().distinct().toList();

		for (Region r : lstRegionsParcourues)
		{
			tempIles = 0;

			for (Ile i : lstIlesParcourues)
			{
				if (r.contien(i))
					tempIles++;
			}

			if (r.equals(this.regionBonus))
				tempIles++;

			if (tempIles > nbMaxIles)
				nbMaxIles = tempIles;
		}

		int score = nbMaxIles * lstRegionsParcourues.size();

		int bonusChemins = 0;
		int bonusIles    = 0;

		for (Chemin c : this.lstCheminColorie)
		{
			bonusChemins += c.getBonus();
			// if (c.getBonus() != 0) System.out.println("Bonus: " + c.getBonus());
		}

		boolean red = false;
		boolean blue = false;

		for (Ile i : lstIlesParcourues)
		{
			List<Chemin> lstChms = i.getCheminAutour();

			red = false;
			blue = false;

			for (Chemin c : lstChms)
			{
				if (!c.estColorie())
					continue;

				if (c.getCouleurPrim().equals(Color.RED))
					red = true;
				
				if (c.getCouleurPrim().equals(Color.BLUE))
					blue = true;
			}

			if (red && blue)
				bonusIles += 2;
		}

		this.points = (score + bonusChemins + bonusIles) + "; bonus chemins: " + bonusChemins + "; bonus îles: " + bonusIles;
	}

	public String getScore() { return this.points; }

	

	public static void            addAction  (Mouvement mv) { Mappe.lstHistorique.add(mv); }
	public static List<Mouvement> getActions ()             { return Mappe.lstHistorique ; }

	public void enregistrerMouvement(Integer score1, Integer score2)
	{
		try
		{
			PrintWriter pw = new PrintWriter( new FileOutputStream(Mappe.NOM_FICHIER + "journal.data") );

			pw.println ("[" + new Date().toString() + "]" );
			pw.println ("(" + this.tempPartie() + ")" );
			
			if (score2 == null)
				pw.println (score1);
			else
				pw.println(score1 + "/" + score2);

			for (Mouvement mv : Mappe.lstHistorique)
				pw.println(mv.toData());

			pw.close();
		}
		catch (Exception e){ e.printStackTrace(); }

	}

	private String tempPartie ()
	{
		long duree = System.currentTimeMillis() - Mappe.debutPartieTemps;
		int  sec   = (int) ( duree / 1000 );

		return String.format("%02d:%02d:%03d", sec/60, sec%60, duree%1000);
	}

	/**                                    */
	/**               Forcage              */
	/**                                    */

	public void lancerScenario (int num)
	{
		try
		{
			Scanner scan = new Scanner(new FileInputStream(Mappe.NOM_FICHIER + "scenarios/scenario_" + num + ".data"), StandardCharsets.UTF_8);

			Controleur.setNbJoueur(Integer.parseInt(scan.nextLine()));

			int nbTour   = 0;
			int nbManche = 0;

			while (scan.hasNextLine()) 
			{
				String s = scan.nextLine();
				String[] ensInfo = s.split("\t");

				// Pioche carte
				if (ensInfo.length == 2)
				{
					Color coulContour;
					if (ensInfo[1].equals("primaire")) coulContour = Color.BLACK;
					else                               coulContour = Color.WHITE;
					
					for (Carte c : Carte.values())
						if (c.getCouleur().equals(ensInfo[0]) && c.getContour() == coulContour)
							this.ctrl.forcePioche(c);
					
					nbTour ++;
				}

				// Si il y a les infos d'une île
				if (ensInfo.length == 4)
				{
					int    numJoueur = Integer.parseInt (ensInfo[0]);
					Ile    ileA      = this.getIleId(ensInfo[1]);
					Ile    ileB      = this.getIleId(ensInfo[2]);
					Chemin c         = this.trouverChemin(ileA, ileB);

					Color coul;
					if (ensInfo[3].equals("rouge")) coul = Color.RED ;
					else                            coul = Color.BLUE;

					this.ctrl.forceColorier(numJoueur, c, coul);
				}

				//Evennement
				if (ensInfo.length == 1) 
				{
					if (ensInfo[0].equals("bifurcation"))
						Mappe.setTourEventBifurcation(nbTour);
					else
						if (ensInfo[0].equals(""))
							this.ctrl.initialiserManche();
						else
						{
							for (CarteBonus cb : CarteBonus.values())
								if (cb.name().equals(ensInfo[0]))
									Mappe.carteBonus = cb;
						}
				}

			}

			scan.close();
		}
		catch (Exception e)
		{
			System.out.println("Nom fichier invalide : " + Mappe.NOM_FICHIER);
		}
	}

	public static void prendreOptionScénario(int num)
	{
		try
		{
			Scanner scan = new Scanner(new FileInputStream(Mappe.NOM_FICHIER + "scenarios/scenario_" + num + ".data"), StandardCharsets.UTF_8);

			String   s = scan.nextLine();
			String[] ensInfo = s.split("\t");

			Controleur.setNbJoueur    (Integer.parseInt(ensInfo[0]));

			if (ensInfo[1].equals("true")) Controleur.setOptionActive(true) ;
			else                           Controleur.setOptionActive(false);

			Controleur.
			
			scan.close();
		}
		catch (Exception e)
		{
			System.out.println("Nom fichier invalide : " + Mappe.NOM_FICHIER);
		}
	}

	public void melangerPioche(boolean etat)
	{
		this.piocheMelangee = etat;
	}

	public static void setTourEventBifurcation(int tour)
	{
		Mappe.tourEventBifurcation = tour;
	}

	public static void forceCarteBonus(CarteBonus cb)
	{
		Mappe.carteBonus = cb;
	}
}
