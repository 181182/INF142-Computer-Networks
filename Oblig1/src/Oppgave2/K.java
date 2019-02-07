package Oppgave2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Dette er en klient til applikasjonprotokolen mellom klient og en web proxy
 * server.
 * 
 * @author Magnus Ødegård Bergersen (huh007) og Jacob Ås (jaa093)
 * 
 */

public class K {

	public static void main(String args[]) {

		/**
		 * Her initialiserer vi alle hjelpeklassene som blir brukt i klassen.
		 */
		DatagramSocket klientSocket = null;
		InetAddress IPaddresse = null;

		/**
		 * Setter hvor mange bytes vi skal reservere til å sende og motta pakker
		 */
		byte[] motattData = new byte[10240];
		byte[] sendData = new byte[10240];

		/**
		 * Vi setter IP og Port til WPS. Setter hvilken url som skal sendes til WPS.
		 */
		String IP = "10.0.0.135";
		int Port = 8887;
		String URL = "https://example.com";

		/**
		 * Her fører du inn IP-addresse til WPS. Tester om IP'en er en gyldig addresse
		 */

		try {
			IPaddresse = InetAddress.getByName(IP);
		} catch (UnknownHostException e1) {
			System.out.println("Ugyldig IP-addresse");
			Thread.currentThread().interrupt();
			return;
		}

		/**
		 * Oppretting av DatagramSocket, og kobler til WPS med tilhørende IP og Port.
		 * Her velger vi å bruke samme port som WPS. Connect-metoden kobler seg til WPS
		 * serveren og gjør det slik at K ikke kan motta data fra andre uønskede
		 * enheter.
		 * 
		 */

		try {
			klientSocket = new DatagramSocket(Port);
		} catch (SocketException e1) {
			System.out.println("Det oppstod en feil ved oppretting av DatagramSocket");
			Thread.currentThread().interrupt();
			return;
		}
		klientSocket.connect(IPaddresse, Port);

		/**
		 * Gjør URL strengen om til bytes. sendPacket pakker inn data den skal sende
		 * samt IP og Port slik at pakken kan ankomme rett enhet. Sender URL i bytes med
		 * eventuelt stinavn til WPSen med UDP sockets.
		 */

		try {
			sendData = URL.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPaddresse, Port);
			klientSocket.send(sendPacket);
		} catch (IOException e) {
			System.out.println("Klarte ikke å sende pakken. Har du brukt ugyldig port?");
			klientSocket.close();
			Thread.currentThread().interrupt();
			return;
		}

		/**
		 * Mottar vi pakke fra WPS som skal inneholde headerinformasjonen til URL vi
		 * sendte. Til slutt printer vi ut det vi har fått fra WPS Vi gir WPS 10
		 * sekunder på å sende en pakke tilbake, dette vil forhindre at hvis pakketap
		 * oppstår vil K ikke vente for alltid
		 * 
		 */

		DatagramPacket mottaPakke = new DatagramPacket(motattData, motattData.length);
		System.out.println("Venter på pakke");
		try {
			klientSocket.setSoTimeout(10000);
			klientSocket.receive(mottaPakke);
			String setning = new String(mottaPakke.getData());
			System.out.println(setning);

		} catch (IOException e) {
			System.out.println("Mottok ingen pakke fra " + IPaddresse);
			klientSocket.close();
			Thread.currentThread().interrupt();
			return;
		}
		klientSocket.close(); // Her lukker vi Socket

	}
}
