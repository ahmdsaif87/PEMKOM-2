import java.io.Serializable;

public class Book implements Serializable {
    private String judul;
    private String penulis;
    private int tahun;
    private String genre;

    public Book(String judul, String penulis, int tahun, String genre) {
        this.judul = judul;
        this.penulis = penulis;
        this.tahun = tahun;
        this.genre = genre;
    }

    public String getJudul() {
        return judul;
    }

    public String getPenulis() {
        return penulis;
    }

    public int getTahun() {
        return tahun;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return "Judul: " + judul + ", Penulis: " + penulis + ", Tahun: " + tahun + ", Genre: " + genre;
    }
}
