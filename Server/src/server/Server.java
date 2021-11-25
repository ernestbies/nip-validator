package server;

import gui.GUI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.regex.Pattern;
import rmi.NIPValidator;

/**
 *
 * @author Ernest Bieś, PWSZ Tarnów 2020
 */
public class Server implements NIPValidator {
    
    private GUI gui;
    
    public Server(GUI g) {
        gui = g;
    }
    
    public void bindStub() {
        try {
            LocateRegistry.createRegistry(1099);
            NIPValidator stub = (NIPValidator) UnicastRemoteObject.exportObject(this, 1099);
            Registry reg = LocateRegistry.getRegistry();
            reg.bind("NIPValidator", stub);
        } catch(Exception e) {
            gui.getTextArea().setText(gui.getTextArea().getText() + "\n>>>>> Wystąpił błąd: " + e.toString());
        }
    }
    
    @Override
    public boolean validate(String nip) throws RemoteException {
        gui.getTextArea().setText(gui.getTextArea().getText() + 
                "\n>>>>> Walidacja numeru NIP została zdalnie wywołana dla NIP = " + nip);
        System.out.println("[LOG] Walidacja numeru NIP została zdalnie wywołana dla NIP = " + nip);
        
        // Zmienne reprezentujące format NIP (z łącznikiem lub bez):
        // 1) Format NIP z łącznikiem
        final String OLD_NIP_FORMAT = "^((\\d{3}[- ]\\d{3}[- ]\\d{2}[- ]\\d{2})|(\\d{3}[- ]\\d{2}[- ]\\d{2}[- ]\\d{3}))$";
        // 2) Format NIP bez łącznika
        final String NEW_NIP_FORMAT = "^[0-9]{10}$";
        
        // Zmienna reprezentująca wagi sumy kontrolnej
        final int[] CHECKSUM_WEIGHTS = {6, 5, 7, 2, 3, 4, 5, 6, 7};
        
        // Zmienna reprezentująca czy suma kontrolna jest prawidłowa
        boolean isChecksumValid;
        
        // Algorytm obliczania sumy kontrolnej oraz sprawdzenia poprawności NIP
        int sum = 0;
        String nipWithoutPrefix = nip.replaceAll("PL", "").trim();
        String nipToCheck = nipWithoutPrefix.replaceAll("-", "");
        try {
            for (int i = 0; i < CHECKSUM_WEIGHTS.length; i++) {
                sum += Integer.parseInt(nipToCheck.substring(i, i+1)) * CHECKSUM_WEIGHTS[i];
            }
        } catch (NumberFormatException ex) {
            System.err.println("[LOG] Podany NIP jest nieprawidłowy. " + ex);
            return false;
        }
	isChecksumValid = (sum % 11) == Integer.parseInt(nipToCheck.substring(9, 10));
        
        // Sprawdzenie za pomocą wyrażenia regularnego czy podany napis to NIP
        Pattern patternOldNipFormat = Pattern.compile(OLD_NIP_FORMAT, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Pattern patternNewNipFormat = Pattern.compile(NEW_NIP_FORMAT, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        
        return (patternOldNipFormat.matcher(nipWithoutPrefix).matches() || patternNewNipFormat.matcher(nipWithoutPrefix).matches()) && isChecksumValid;
    }
}
