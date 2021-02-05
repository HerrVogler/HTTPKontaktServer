//Jakob Vogler

public class Kontakt {
    private String vorname, nachname;
    private boolean istPrivat;

    public Kontakt(String vn, String nn, boolean iP) {
        vorname = vn;
        nachname = nn;
        istPrivat = iP;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public boolean istPrivat() {
        return istPrivat;
    }

    public void setIstPrivat(boolean istPrivat) {
        this.istPrivat = istPrivat;
    }
}
