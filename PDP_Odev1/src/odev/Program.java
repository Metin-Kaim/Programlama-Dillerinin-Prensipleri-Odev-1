/**
*
* @author Metin Kaim  metin.kaim@ogr.sakarya.edu.tr
* @since 08.40.2023
* <p>
* Consoldan verilen *.java adlı dosyayı okuyarak içindeki fonksiyonları buluyor. 
* Ardından yorum satırlarını bulup fonksiyonları ve yorum satırlarını ilişkilendirip ilgili yerlere yazıyor.
* </p>
*/

package odev;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class Program {
	public enum SatirIsimleri {
		javadoc, cokSatirli, tekSatirli
	}

	public static void main(String[] args) throws IOException {

		Program pr = new Program();

		ArrayList<String> funcsList = new ArrayList<String>();// fonksiyonlar listeye eklendi -DONE-

		ArrayList<ArrayList<ArrayList<String>>> generalCommentsList = new ArrayList<ArrayList<ArrayList<String>>>(); // 0-javadoc,
																														// 1-cok,
																														// 2-tek
		String className = "";

		String fileName = args[0];

//----------------------------------------------------------------sinif_adini_alma----------------------------------------------------------------
		className = pr.getClassName(fileName);
//----------------------------------------------------------------fonksiyonlari_alma----------------------------------------------------------------
		funcsList = pr.getFunctionNames(fileName);
//----------------------------------------------------------------yorum_satirlarini_alma----------------------------------------------------------------
		generalCommentsList = pr.getComments(fileName);
//----------------------------------------------------------------dosyaya_yazma----------------------------------------------------------------
		pr.writeOnText(funcsList, generalCommentsList);
// ----------------------------------------------------------------consola_yazma----------------------------------------------------------------
		pr.writeOnConsole(className, funcsList, generalCommentsList);

	}// main

	private String getClassName(String fileName) throws IOException {
		try (BufferedReader r = new BufferedReader(new FileReader(fileName))) {

			String line;

			while ((line = r.readLine()) != null) {// satir alindi
				Pattern pattern = Pattern.compile("(?<=class )\\w*");// class regex tanimi
				Matcher matcher = pattern.matcher(line);

				if (matcher.find()) {
					return matcher.group();// class adini dön
				}
			}
			return null;
		}
	}

	private ArrayList<String> getFunctionNames(String fileName) throws IOException {

		int funcAlgilayici = 0;// her bir süslü paranteze bağlı olarak degeri degisiyor ve fonksiyonların
								// algınalmasını sagliyor.
		boolean girebilirMi = true;

		ArrayList<String> funcsList = new ArrayList<String>();

		try (BufferedReader r = new BufferedReader(new FileReader(fileName))) {

			String line;

			while ((line = r.readLine()) != null) {

				if (Pattern.matches(".*\\{.*", line)) {
					funcAlgilayici++;
				} else if (Pattern.matches(".*\\}.*", line)) {
					funcAlgilayici--;
				}
				if (funcAlgilayici == 1)// yeni bir fonksiyon algilandi
					girebilirMi = true;

				if (funcAlgilayici == 2 && girebilirMi) {// fonksiyon adini alna
					girebilirMi = false;
					Pattern pattern = Pattern.compile("\\w*(?=\\(\\w* ?\\w*\\) ?\\{)");// fonksiyon regex tanimi
					Matcher matcher = pattern.matcher(line);

					if (matcher.find()) {
						funcsList.add(matcher.group());
					}
				}
			}
			r.close();
			return funcsList;
		}
	}

	private ArrayList<ArrayList<ArrayList<String>>> getComments(String fileName) throws IOException {

		ArrayList<ArrayList<ArrayList<String>>> generalList = new ArrayList<ArrayList<ArrayList<String>>>();
		generalList.add(new ArrayList<>());// javadoc
		generalList.add(new ArrayList<>());// cokSatir
		generalList.add(new ArrayList<>());// tekSatir

		int funcAlgilayici = 0;
		boolean girebilirMi = false;
		int sayac = 0; // yorumların tutulduğu listelerin içinden istenen listeye erişimi sağlama
						// (List{List{0},List{1},List{2},List{3},List{4}})

		try (BufferedReader r = new BufferedReader(new FileReader(fileName))) {
			generalList.get(SatirIsimleri.javadoc.ordinal()).add(new ArrayList<>());
			generalList.get(SatirIsimleri.cokSatirli.ordinal()).add(new ArrayList<>());
			generalList.get(SatirIsimleri.tekSatirli.ordinal()).add(new ArrayList<>());
			String line;
			while ((line = r.readLine()) != null) {
				line = line.trim();
				if (Pattern.matches(".*\\{.*", line)) {
					funcAlgilayici++;
				} else if (Pattern.matches(".*\\}.*", line)) {
					funcAlgilayici--;
					girebilirMi = true;
				}
				if (funcAlgilayici == 1 && girebilirMi) {// her fonksiyon bitimi
					girebilirMi = false;
					generalList.get(SatirIsimleri.javadoc.ordinal()).add(new ArrayList<>());
					generalList.get(SatirIsimleri.cokSatirli.ordinal()).add(new ArrayList<>());
					generalList.get(SatirIsimleri.tekSatirli.ordinal()).add(new ArrayList<>());
					sayac++;
				} // Her fonksiyon bittiğinde listelere yeni alt listeler ekliyor ve böylelikle
					// yorum satırları fonksiyonlara göre ayrılmış oluyor.

				// ----------------------------------------------Tek_Satirli_Yorumlar----------------------------------------------

				Pattern pattern1 = Pattern.compile("(?<=\\/\\/).+");// tek // yorum satırı içi --DONE--
				Matcher matcher1 = pattern1.matcher(line);

				if (matcher1.find()) {
					generalList.get(SatirIsimleri.tekSatirli.ordinal()).get(sayac).add(matcher1.group());
				}

				// ----------------------------------------------Cok_Satirli_Yorumlar----------------------------------------------

				Pattern pattern5 = Pattern.compile("(?<=\\/\\*)[ \\wçüşıö]+");// cok /*-*/ yorum satırı içi// --DONE--

				Matcher matcher5 = pattern5.matcher(line);

				if (matcher5.find()) {

					generalList.get(SatirIsimleri.cokSatirli.ordinal()).get(sayac).add(matcher5.group());
				} else {
					if (Pattern.matches("\\/\\* *", line)) {
						String coksatirString = "";// coklu yorum satirini tek satir olarak alma

						while ((line = r.readLine()) != null) {
							line = line.trim();
							if (Pattern.matches("\\* *", line))// eger ki *'dan sonrası boşsa pas geciyor
								continue;
							else if (Pattern.matches("\\*\\/ *", line)) {// yorum satırının bitişi
								generalList.get(SatirIsimleri.cokSatirli.ordinal()).get(sayac).add(coksatirString);// yorum_satırının_içini_listeye_ekleme
								break;
							}

							Pattern pattern2 = Pattern.compile("(?<=\\*).+");// cok /*-*/ yorum satırı içi

							Matcher matcher2 = pattern2.matcher(line);

							if (matcher2.find()) {
								coksatirString += matcher2.group();// aynı yorum satırı içinde olan ifadeleri tek bir
																	// string ifadede toplama
							}
						}
					}
				}

				// ----------------------------------------------Javadoc_Yorumlar----------------------------------------------

				Pattern pattern6 = Pattern.compile("(?<=\\/\\*\\*)[ \\wçüşıö]+");// javadoc_/**-*/_yorum_satırı_içi_--DONE--

				Matcher matcher6 = pattern6.matcher(line);

				if (matcher6.find()) {// tek satirda javadoc /** ... */
					generalList.get(SatirIsimleri.javadoc.ordinal()).get(sayac).add(matcher6.group());
				} else {

					if (Pattern.matches("\\/\\*\\* *", line)) {// coklu javadoc tespit /** \n*..\n* */

						String javadocString = "";

						while ((line = r.readLine()) != null) {// bir sonraki satira gec
							line = line.trim();
							if (Pattern.matches("\\* *", line))// eger ki *'dan sonrası boşsa pas geciyor
								continue;
							else if (Pattern.matches("\\*\\/ *", line)) {// yorum satırının bitişi
								generalList.get(SatirIsimleri.javadoc.ordinal()).get(sayac).add(javadocString);// yorum_satırının_içini_listeye_ekleme
								break;
							}

							Pattern pattern2 = Pattern.compile("(?<=\\* *).+");// javadoc /**-*/ yorum satırı içi

							Matcher matcher2 = pattern2.matcher(line);

							if (matcher2.find()) {
								javadocString += matcher2.group();
							}
						}
					}
				}
				// -----------------------------------------------------------------------
			} /// try while
			r.close();
		} // try
		return generalList;
	}

	private void writeOnText(ArrayList<String> funcsList, ArrayList<ArrayList<ArrayList<String>>> generalCommentsList)
			throws IOException {
		writeOnTekSatirText(funcsList, generalCommentsList.get(SatirIsimleri.tekSatirli.ordinal()));
		writeOnCokSatirText(funcsList, generalCommentsList.get(SatirIsimleri.cokSatirli.ordinal()));
		writeOnJavadocText(funcsList, generalCommentsList.get(SatirIsimleri.javadoc.ordinal()));
	}

	private void writeOnTekSatirText(ArrayList<String> funcsList, ArrayList<ArrayList<String>> tekSatirList)
			throws IOException {
		File tekFile = new File("teksatir.txt");// dosya olusturma
		if (!tekFile.exists()) {
			tekFile.createNewFile();
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(tekFile))) {// dosyaya yazma
			for (int i = 0; i < funcsList.size(); i++) {

				bw.write("Fonksiyon: " + funcsList.get(i) + "\n\n");// fonksiyon adi

				for (int j = 0; j < tekSatirList.get(i).size(); j++) {// eger ki fonksiyona ait bir yorum varsa calis

					bw.write("  " + tekSatirList.get(i).get(j) + "\n");// fonksiyona ait yorumlari listele
				}
				bw.write("\n------------------------------\n\n");
			}
			bw.close();
		}
	}

	private void writeOnCokSatirText(ArrayList<String> funcsList, ArrayList<ArrayList<String>> cokSatirList)
			throws IOException {
		File cokFile = new File("coksatir.txt");// dosya olusturma
		if (!cokFile.exists()) {
			cokFile.createNewFile();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(cokFile))) {// dosyaya yazma
			for (int i = 0; i < funcsList.size(); i++) {

				bw.write("Fonksiyon: " + funcsList.get(i) + "\n\n");// fonksiyon adi

				for (int j = 0; j < cokSatirList.get(i).size(); j++) {// eger ki fonksiyona ait bir yorum varsa calis

					bw.write("  " + cokSatirList.get(i).get(j) + "\n");// fonksiyona ait yorumlari listele
				}
				bw.write("\n------------------------------\n\n");
			}
			bw.close();
		}
	}

	private void writeOnJavadocText(ArrayList<String> funcsList, ArrayList<ArrayList<String>> javadocList)
			throws IOException {
		File javadocFile = new File("javadoc.txt");// dosya olusturma
		if (!javadocFile.exists()) {
			javadocFile.createNewFile();
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(javadocFile))) {// dosyaya yazma
			for (int i = 0; i < funcsList.size(); i++) {

				bw.write("Fonksiyon: " + funcsList.get(i) + "\n\n");// fonksiyon adi

				for (int j = 0; j < javadocList.get(i).size(); j++) {// eger ki fonksiyona ait bir yorum varsa calis

					bw.write("  " + javadocList.get(i).get(j) + "\n");// fonksiyona ait yorumlari listele
				}
				bw.write("\n------------------------------\n\n");
			}
			bw.close();
		}
	}

	private void writeOnConsole(String className, ArrayList<String> funcsList,
			ArrayList<ArrayList<ArrayList<String>>> generalCommentsList) {

		System.out.println("Sinif: " + className);

		for (int i = 0; i < funcsList.size(); i++) {
			System.out.println("	Fonksiyon: " + funcsList.get(i));

			System.out.println(String.format("		Tek Satir Yorum Sayisi:%5d",
					generalCommentsList.get(SatirIsimleri.tekSatirli.ordinal()).get(i).size()));

			System.out.println(String.format("		Cok Satirli Yorum Sayisi:%3d",
					generalCommentsList.get(SatirIsimleri.cokSatirli.ordinal()).get(i).size()));

			System.out.println(String.format("		Javadoc Yorum Sayisi:%7d",
					generalCommentsList.get(SatirIsimleri.javadoc.ordinal()).get(i).size()));
		}
	}

}// class
