package Oppgave3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;

/*
 * 
 * Laget av Magnus Ødegård Bergersen (huh007) og Jacob Ås (jaa039)
 * 
 * */

public class WPS {
	public static void main(String args[]) {

		/*
		 * Her initialiserer vi alle hjelpeklassene som blir brukt i klassen.
		 */
		DatagramSocket datagramsocket = null;
		Socket socket = null;
		PrintWriter printwriter = null;
		BufferedReader bufferedreader = null;
		InetAddress ipaddress = null;
		URL url = null;
		DatagramPacket datagrampacket = null;

		/*
		 * Her oppretter vi en ny datagramsocket med porten 8887 som parameter som
		 * binder socketen til den gitte porten på Web-Proxy-Server maskinen. Man
		 * oppretter også to lister med bytes som kan brukes til å hente og sende data
		 * igjennom datagramsocketen. Ved bruk av to lister klarer man lettere å holde
		 * kontroll på hvilke pakker som blir sendt og hentet og forhindrer at den ene
		 * plutselig skal skrive over den andre. Også ved bruk av store pakker fram og
		 * tilbake (selv om det hadde vært mulig å sende en pakke gitt størrelsen til
		 * teksten).
		 */

		try {
			datagramsocket = new DatagramSocket(8887);
		} catch (SocketException e) {
			/*
			 * Ettersom vi forsikrer oss at koden stopper her, trenger ikke socketen å
			 * lukkes.
			 */
			System.out.println("Det oppstod en feil med danning av socketen på denne porten.");
			Thread.currentThread().interrupt();
			return;
		}
		byte[] mottadata = new byte[10240];
		byte[] sendedata = new byte[10240];

		/*
		 * Variabler som skal bli brukt og er der for å gjøre mer tilgjengelig til
		 * seinere tid.
		 */
		String setning = null;
		int HTTP = 80;

		/*
		 * Her loopes datagramsocketen til den har motatt rett URL og har klart å sende
		 * tilbake informasjon uten feil.
		 */
		ferdig: while (true) {

			/*
			 * Her lages datagrampakkene som brukes for å sende informasjon mellom klient og
			 * WPS. Datagrampakken inneholder all informasjon til å bli videresendt til rett
			 * plass ved hjelp av UDP. Dette vil også si at det ikke alltid er sjangs for at
			 * pakken kommer fram til enheten.
			 */
			datagrampacket = new DatagramPacket(mottadata, mottadata.length);
			try {
				datagramsocket.receive(datagrampacket);
			} catch (IOException e) {
				/*
				 * Om det oppstår feil vil hele tråden til programmet avsluttes med
				 * Thread.Interrupt. Dette forhindrer at programmet prøver å kjøre selv om den
				 * ikke har klart å åpne noen av socketene.
				 */
				System.out.println("Det oppstod en feil med å hente informasjon.");
				Thread.currentThread().interrupt();
				return;
			}
			setning = new String(datagrampacket.getData());

			/*
			 * Hver datagrampakke som blir sendt inneholder porten og IPen fra enheten som
			 * sendte dem. Ved bruk av dette kan vi hente denne infoen og videre sende et
			 * svar tilbake til den enheten som sendte den.
			 */
			ipaddress = datagrampacket.getAddress();
			int port = datagrampacket.getPort();

			System.out.println("Nettside som er sendt: " + setning);

			/*
			 * For å forhindre nullpointexceptions tester vi her om det som er sendt om det
			 * faktisk inneholder noe informasjon.
			 */
			ifsetning: if (setning != null) {

				/*
				 * Når man har fått bekreftet at setningen som er sendt inneholder noe kan vi
				 * videre se om det faktisk er en URL. Ved sending av feil URL vil det oppstå en
				 * feil som try/catch setningen fanger opp og videre sender tilbakemelding
				 * tilbake til enheten som sendte meldingen.
				 */
				try {

					/*
					 * Ved bruk av hjelpeklassen URL kan vi teste om meldingen som er sendt faktisk
					 * er et domene på nettet hvor man kan hente en HTTP melding fra.
					 */
					url = new URL(setning);

					/*
					 * Om ikke URL klassen oppdager noen feil kan vi videre ta å opprette en socket
					 * som skal hente informasjon fra en nettside. Under dannes en socket som skal
					 * fungere som et endepunkt mellom WPSen og nettsiden. Ved bruk av
					 * InetAddress.getByName og URL sin hjelpemetoden .getHost kan vi da splitte opp
					 * URLen som har blitt tilsendt på en måte hvor man kan få IPaddressen til
					 * serveren som har nettsiden som kan brukes til å hente informasjon fra.
					 */
					socket = new Socket(InetAddress.getByName(url.getHost()), HTTP);
					printwriter = new PrintWriter(socket.getOutputStream());

					System.out.println("Host: " + url.getHost());

					/*
					 * Om informasjonen vi henter ikke inneholder noen form for stinavn så har vi
					 * laget en metode for å forsikre oss at man henter informasjon fra default
					 * stinavn. Ikke alle nettsider gir default stinavn ved henting av informasjon.
					 * Med denne koden (eksempel vg.no) og dette vil da forhindre programmet i å
					 * hente noen informasjon i det heile tatt.
					 */
					if (url.getPath() == "") {
						printwriter.print("GET / HTTP/1.1\r\n");
						System.out.println("Stinavn: /");
					} else {

						/*
						 * Ved URL hjelpeklassen man kan splitte URIen fra den hentede setningen slik at
						 * man får hentet den nettsiden som brukeren vil ha informasjon fra. Dette må
						 * gjøres i get-requesten ettersom hostnavnet til domene allerede blir sendt ved
						 * HTTP setningen Host:.
						 */
						printwriter.print("GET " + url.getPath() + " HTTP/1.1\r\n");
						System.out.println("Stinavn: " + url.getPath());

					}

					/*
					 * Når socketen kobler seg til et domene som lytter etter meldinger med port 80
					 * vil man da klare å hente informasjon fra nettsiden. Ved å sende en GET
					 * forespørsel med stinavn og hostnavnet (en webserver kan hoste flere domener
					 * samtidig) samt en blank linje, vil man da få tilsendt tilbake en HTTP
					 * response.
					 */
					printwriter.println("Host: " + url.getHost() + "\r\n\r\n");
					printwriter.flush();
					bufferedreader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					String tekst = "";
					String t = "";

					/*
					 * Denne loopen konverterer hele headeren fra nettsiden til en enkel string som
					 * kan sendes til klient. Ettersom hver HTTP request alltid har en blank linje
					 * mellom html koden og http header kan vi da skille ut headeren fra koden ved å
					 * fange den opp.
					 */
					loop: while ((t = bufferedreader.readLine()) != null) {
						tekst = tekst + t + " ";

						if (t.equals("")) {
							break loop;
						}

					}

					/*
					 * På grunn av koden vår ikke tar hensyn til at informasjonen blir sendt kommer
					 * fram til klient så har vi fysisk lagt til et sekund delay før den blir
					 * videresendt tilbake til klient slik at enheten skal være klar til å ta imot
					 * informasjon.
					 */
					System.out.println(tekst);
					System.out.println("Sender pakke om 1 sekund!");
					Thread.sleep(1000);

					/*
					 * Her konverteres headeren til bytes og blir sendt tilbake til klienten og
					 * deretter lukkes datagramsocketen for ressursene deres kan brukes til videre
					 * bruk av andre applikasjoner.
					 */
					sendedata = tekst.getBytes();
					datagrampacket = new DatagramPacket(sendedata, sendedata.length, ipaddress, port);
					datagramsocket.send(datagrampacket);
					datagramsocket.close();
					break ferdig;
				}

				/*
				 * Om URL hjelpeklassen ikke klarer å konvertere teksten til en URL vil den da
				 * gi en exception som vi plukker opp her og sender tilbakemelding tilbake til
				 * klient at programmet ikke klarte å lese nettsiden som ble sendt.
				 */
				catch (Exception E) {
					System.out.println("Ugyldig nettside, prøv igjen");
					sendedata = "Ugyldig nettside, prøv igjen. Ps: Har du husket https:// eller http://?".getBytes();
					datagrampacket = new DatagramPacket(sendedata, sendedata.length, ipaddress, port);
					try {
						datagramsocket.send(datagrampacket);
					} catch (IOException e) {
						System.out.println("Det oppstod et problem ved sending av pakken tilbake til klienten.");
						Thread.currentThread().interrupt();
						return;
					}
					System.out.println("-- Prøv igjen --");
					break ifsetning;
				}

			}
			/*
			 * Her reseter vi bytelisten slik at man forsikrer seg at teksten man henter er
			 * helt ny.
			 */
			mottadata = new byte[mottadata.length];

		}

		/*
		 * Her lukkes alle hjelpeklasse slik at de ikke skal brukes lengre. Spesielt
		 * viktig når det brukes socketer som skal kommunisere ved hjelp av TCP,
		 * ettersom et annet program kan ikke bruke den samme porten som socketen
		 * bruker.
		 */
		System.out.println("Ferdig.");
		try {
			bufferedreader.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("Det oppstod et problem med å lukke bufferreader eller socketen. ");
			Thread.currentThread().interrupt();
			return;
		}

		printwriter.close();

	}
}