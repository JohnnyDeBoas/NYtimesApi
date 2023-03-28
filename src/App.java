import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import javax.swing.JOptionPane;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class App {

    public static void main(String[] args) throws Exception {
        String nameAuthor = JOptionPane.showInputDialog("Digite o nome do autor");
        if (nameAuthor == null) {
            System.out.println("Pesquisa cancelada\u001B[0m");
        } else {
            if (!nameAuthor.isEmpty()) {
                App app = new App();
                String[] author = app.nameToRequest(nameAuthor);
                JSONArray results = app.returnAPI("https://api.nytimes.com/svc/books/v3/reviews.json?author=",
                        author[0]);
                if (!results.isEmpty()) {
                    System.out.println(
                            "\u001B[0mAqui estão os resultados dos livros de \033[34m" + author[1] + "\u001B[0m");
                    JSONArray books = new JSONArray();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject book = results.getJSONObject(i);
                        String name = book.getString("book_title");
                        String publiDate = app.convertDate(book.getString("publication_dt"));

                        JSONObject bookObject = new JSONObject();
                        bookObject.put("id", i);
                        bookObject.put("name", name);
                        books.put(bookObject);
                        System.out.println("\u001B[0mCodigo: \033[1m\033[36m" + i + "\u001B[0m Nome: \033[1m\033[36m"
                                + name + "\u001B[0m -  Data de lançamento: \033[36m" + publiDate + "\u001B[0m");
                    }
                    String infoBook = JOptionPane.showInputDialog(
                            "Gostaria de ver comentarios sobre alguma livro? se sim digite o codigo do livro");
                    if (infoBook == null) {
                        System.out.println("Obrigado e até a proxima");
                    } else {
                        int codBook = Integer.parseInt(infoBook);
                        if (codBook > 1) {
                            String[] bookRequest = new String[2];
                            for (int i = 0; i < books.length(); i++) {
                                JSONObject book = books.getJSONObject(i);
                                int id = book.getInt("id");
                                if (id == codBook) {
                                    // Aqui é o codigo de procuro do livro
                                    String name = book.getString("name");
                                    bookRequest =  app.nameToRequest(name);
                                    break;
                                }
                            }
                            if(bookRequest != null || bookRequest.length > 0){
                                JSONArray bookResult = app.returnAPI("https://api.nytimes.com/svc/books/v3/reviews.json?title=", bookRequest[0]);
                                if(!bookResult.isEmpty()) {
                                    System.out.println(
                                            "\u001B[0mAqui estão informações de \033[34m" + bookRequest[1] + "\u001B[0m de " + author[1]);
                                    
                                    JSONArray bookInfos = new JSONArray();
                                    if(bookResult.length() > 1){
                                        for (int index = 0; index < bookResult.length(); index++) {
                                            JSONObject  curretBook = bookResult.getJSONObject(index);
                                            if(!curretBook.getString("summary").isEmpty() ){
                                                String byline = curretBook.getString("byline");
                                                String summary = curretBook.getString("summary");
                                                String publiDate = app.convertDate(curretBook.getString("publication_dt"));
                                                JSONObject bookInfo = new JSONObject();

                                                bookInfo.put("byline", byline);
                                                bookInfo.put("summary", summary);
                                                bookInfo.put("publiDate", publiDate);

                                                bookInfos.put(bookInfo);
                                            }
                                        }
                                    } else {
                                        JSONObject curretBook = bookResult.getJSONObject(0);
                                        String byline = curretBook.getString("byline");
                                        String summary = curretBook.getString("summary");
                                        String publiDate = app.convertDate(curretBook.getString("publication_dt"));
                                        JSONObject bookInfo = new JSONObject();
                                        
                                        bookInfo.put("byline", byline);
                                        bookInfo.put("summary", summary);
                                        bookInfo.put("publiDate", publiDate);

                                        bookInfos.put(bookInfo);
                                    }

                                    for (int i = 0; i < bookInfos.length(); i++) {
                                        JSONObject curretBook = bookInfos.getJSONObject(i);
                                        System.out.println("\033[34mData de publicação: \u001B[0m" + curretBook.getString("publiDate"));
                                        System.out.println("\033[34mComentarista: \u001B[0m" + curretBook.getString("byline"));
                                        System.out.println("\033[34mComentario: \u001B[0m" + curretBook.getString("summary"));
                                        System.out.println("------------------------------------------------\n");
                                    }
                                }
                            } else {
                                System.out.println("Livro não encontrado");
                            }
                        }
                    }
                } else {
                    System.out.println("\033[33mNão foram encontrados livros de \033[36m" + author[1]
                            + "\033[33m, verifique se o nome está escrito corretamente ou se ele consta na lista\u001B[0m");
                }
            } else {
                System.out.print("\033[31mPara pesquisar, o campo não pode estar vazio!\u001B[0m");
            }
        }
    }

    public JSONArray returnAPI(String urlAPi, String shearch) throws InterruptedException {
        String url = urlAPi + shearch + "&api-key=";
        String apiKey = "Rmpx5XWJrWBxOuQZUbUIPNIm7AoNKJbb";
        HttpClient cliente = HttpClient.newHttpClient();
        HttpRequest requisicao = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url + apiKey))
                .build();
        HttpResponse<String> resposta;
        try {
            resposta = cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.err.println("Erro ao enviar requisição HTTP: " + e.getMessage());
            return null; // ou lançar uma exceção personalizada
        }
        String json = resposta.body();
        JSONObject jsnobject = new JSONObject(json);
        JSONArray results = jsnobject.getJSONArray("results");
        return results;
    }

    public String[] nameToRequest(String string) throws InterruptedException {
        String[] names = string.split(" ");
        String requestAuthor = "";
        String fullName = "";
        if (names.length > 1) {
            for (int i = 0; i < names.length; i++) {
                names[i] = names[i].substring(0, 1).toUpperCase() + names[i].substring(1);
                fullName += names[i] + " ";
            }
            for (int i = 0; i < names.length; i++) {
                requestAuthor += names[i] + "+";
            }
        } else {
            requestAuthor += names[0].substring(0, 1).toUpperCase() + names[0].substring(1);
            fullName += names[0];
        }
        return new String[] { requestAuthor, fullName };

    }

    public String convertDate(String date) throws ParseException{
        SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatOut = new SimpleDateFormat("dd/MM/yyyy");
        Date newDate = formatIn.parse(date);
        String returnDate = formatOut.format(newDate);
        return returnDate;
    }

}