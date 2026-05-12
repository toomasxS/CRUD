import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class App {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // HOME 

        server.createContext("/", exchange -> {

            String html = """
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <title>Sistema de Clientes</title>

                        <style>

                            body{
                                font-family: Arial;
                                background:#f5f6fa;
                                margin:0;
                                text-align:center;
                            }

                            header{
                                background:#2f3640;
                                color:white;
                                padding:20px;
                            }

                            .container{
                                margin-top:50px;
                            }

                            .card{
                                display:inline-block;
                                width:220px;
                                background:white;
                                padding:30px;
                                margin:20px;
                                border-radius:10px;
                                box-shadow:0 2px 8px rgba(0,0,0,0.1);
                            }

                            .card a{
                                display:block;
                                margin-top:15px;
                                text-decoration:none;
                                background:#40739e;
                                color:white;
                                padding:10px;
                                border-radius:5px;
                            }

                            .card a:hover{
                                background:#273c75;
                            }

                        </style>

                    </head>

                    <body>

                        <header>
                            <h1>Sistema de Gestão</h1>
                            <p>Java + MySQL</p>
                        </header>

                        <div class="container">

                            <div class="card">
                                <h3>Clientes</h3>
                                <p>Ver clientes</p>
                                <a href="/clientes">Abrir</a>
                            </div>

                            <div class="card">
                                <h3>Novo Cliente</h3>
                                <p>Adicionar cliente</p>
                                <a href="/novo">Criar</a>
                            </div>

                            <div class="card">
                                <h3>Produtos</h3>
                                <p>Ver produtos</p>
                                <a href="/produtos">Abrir</a>
                            </div>

                            <div class="card">
                                <h3>Novo Produto</h3>
                                <p>Adicionar produto</p>
                                <a href="/novoProduto">Criar</a>
                            </div>

                        </div>

                    </body>
                    </html>
                    """;

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

            exchange.sendResponseHeaders(200, html.getBytes().length);

            exchange.getResponseBody().write(html.getBytes());

            exchange.close();

        });

        //CLIENTES

        server.createContext("/clientes", exchange -> {

            StringBuilder html = new StringBuilder();

            html.append("""
                    <html>
                    <head>
                    <meta charset="UTF-8">

                    <style>

                        body{
                            font-family:Arial;
                        }

                        table{
                            border-collapse:collapse;
                            width:100%;
                        }

                        th,td{
                            border:1px solid #ccc;
                            padding:8px;
                            text-align:left;
                        }

                        th{
                            background:#f4f4f4;
                        }

                        a{
                            text-decoration:none;
                            margin-right:10px;
                        }

                    </style>

                    </head>

                    <body>

                    <h2>Lista de Clientes</h2>

                    <a href='/novo'>+ Novo Cliente</a>

                    <br><br>

                    <table>

                    <tr>
                        <th>ID</th>
                        <th>NIF</th>
                        <th>Nome</th>
                        <th>Email</th>
                        <th>Telefone</th>
                        <th>Ações</th>
                    </tr>
                    """);

            try {

                Connection con = LigacaoBD.ligar();

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("SELECT * FROM clientes");

                while (rs.next()) {

                    int id = rs.getInt("id");
                    String nif = rs.getString("nif");
                    String nome = rs.getString("nome");
                    String email = rs.getString("email");
                    String telefone = rs.getString("telefone");

                    html.append("<tr>");
                    html.append("<td>").append(id).append("</td>");
                    html.append("<td>").append(nif).append("</td>");
                    html.append("<td>").append(nome).append("</td>");
                    html.append("<td>").append(email).append("</td>");
                    html.append("<td>").append(telefone).append("</td>");

                    html.append("<td>");

                    html.append("<a href='/editar?id=")
                            .append(id)
                            .append("'>Editar</a>");

                    html.append("<a href='/apagar?id=")
                            .append(id)
                            .append("' onclick=\"return confirm('Eliminar cliente?')\">Apagar</a>");

                    html.append("</td>");

                    html.append("</tr>");
                }

                rs.close();
                st.close();
                con.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

            html.append("""
                    </table>
                    </body>
                    </html>
                    """);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

            exchange.sendResponseHeaders(200, html.toString().getBytes().length);

            exchange.getResponseBody().write(html.toString().getBytes());

            exchange.close();

        });

        // NOVO CLIENTE

        server.createContext("/novo", exchange -> {

            String html = """
                    <html>

                    <head>

                    <meta charset="UTF-8">

                    <style>

                        body{
                            font-family:Arial;
                        }

                        form{
                            width:300px;
                        }

                        input{
                            width:100%;
                            padding:8px;
                            margin-bottom:10px;
                        }

                    </style>

                    </head>

                    <body>

                    <h2>Novo Cliente</h2>

                    <a href='/clientes'>← Voltar</a>

                    <br><br>

                    <form method='POST' action='/guardar'>

                        NIF:
                        <input name='nif'>

                        Nome:
                        <input name='nome' required>

                        Email:
                        <input name='email' type='email' required>

                        Telefone:
                        <input name='telefone'>

                        <button type='submit'>Guardar</button>

                    </form>

                    </body>
                    </html>
                    """;

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");

            exchange.sendResponseHeaders(200, html.getBytes().length);

            exchange.getResponseBody().write(html.getBytes());

            exchange.close();

        });

        // GUARDAR CLIENTE

        server.createContext("/guardar", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                exchange.sendResponseHeaders(405, -1);

                return;
            }

            try {

                String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");

                String[] params = body.split("&");

                String nif = "";
                String nome = "";
                String email = "";
                String telefone = "";

                for (String p : params) {

                    String[] kv = p.split("=");

                    if (kv.length == 2) {

                        String key = kv[0];

                        String value = java.net.URLDecoder.decode(kv[1], "UTF-8");

                        switch (key) {

                            case "nif":
                                nif = value;
                                break;

                            case "nome":
                                nome = value;
                                break;

                            case "email":
                                email = value;
                                break;

                            case "telefone":
                                telefone = value;
                                break;
                        }
                    }
                }

                Connection con = LigacaoBD.ligar();

                String sql = "INSERT INTO clientes(nif,nome,email,telefone) VALUES(?,?,?,?)";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, nif);
                ps.setString(2, nome);
                ps.setString(3, email);
                ps.setString(4, telefone);

                ps.executeUpdate();

                ps.close();
                con.close();

                exchange.getResponseHeaders().add("Location", "/clientes");

                exchange.sendResponseHeaders(302, -1);

                exchange.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

        });

        // EDITAR CLIENTE

        server.createContext("/editar", exchange -> {

            StringBuilder html = new StringBuilder();

            try {

                String query = exchange.getRequestURI().getQuery();

                int id = Integer.parseInt(query.split("=")[1]);

                Connection con = LigacaoBD.ligar();

                String sql = "SELECT * FROM clientes WHERE id=?";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setInt(1, id);

                ResultSet rs = ps.executeQuery();

                if (rs.next()) {

                    html.append("""
                            <html>
                            <head>
                            <meta charset="UTF-8">
                            </head>
                            <body>

                            <h2>Editar Cliente</h2>

                            <form method='POST' action='/atualizar'>
                            """);

                    html.append("<input type='hidden' name='id' value='")
                            .append(id)
                            .append("'>");

                    html.append("NIF:<br>");
                    html.append("<input name='nif' value='")
                            .append(rs.getString("nif"))
                            .append("'><br><br>");
                    html.append("Nome:<br>");
                    html.append("<input name='nome' value='")
                            .append(rs.getString("nome"))
                            .append("'><br><br>");
                    html.append("Email:<br>");
                    html.append("<input name='email' value='")
                            .append(rs.getString("email"))
                            .append("'><br><br>");
                    html.append("Telefone:<br>");
                    html.append("<input name='telefone' value='")
                            .append(rs.getString("telefone"))
                            .append("'><br><br>");
                    html.append("""
                            <button type='submit'>Atualizar</button>

                            </form>

                            </body>
                            </html>
                            """);
                }

                rs.close();
                ps.close();
                con.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.toString().getBytes().length);
            exchange.getResponseBody().write(html.toString().getBytes());
            exchange.close();

        });

        //  ATUALIZAR CLIENTE

        server.createContext("/atualizar", exchange -> {

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {

                exchange.sendResponseHeaders(405, -1);

                return;
            }

            try {

                String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");

                String[] params = body.split("&");

                String id = "";
                String nif = "";
                String nome = "";
                String email = "";
                String telefone = "";

                for (String p : params) {

                    String[] kv = p.split("=");

                    if (kv.length == 2) {

                        String key = kv[0];

                        String value = java.net.URLDecoder.decode(kv[1], "UTF-8");

                        switch (key) {

                            case "id":
                                id = value;
                                break;

                            case "nif":
                                nif = value;
                                break;

                            case "nome":
                                nome = value;
                                break;

                            case "email":
                                email = value;
                                break;

                            case "telefone":
                                telefone = value;
                                break;
                        }
                    }
                }

                Connection con = LigacaoBD.ligar();

                String sql = "UPDATE clientes SET nif=?, nome=?, email=?, telefone=? WHERE id=?";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, nif);
                ps.setString(2, nome);
                ps.setString(3, email);
                ps.setString(4, telefone);
                ps.setInt(5, Integer.parseInt(id));

                ps.executeUpdate();

                ps.close();
                con.close();

                exchange.getResponseHeaders().add("Location", "/clientes");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

        });

        //  APAGAR CLIENTE 

        server.createContext("/apagar", exchange -> {

            try {

                String query = exchange.getRequestURI().getQuery();

                int id = Integer.parseInt(query.split("=")[1]);

                Connection con = LigacaoBD.ligar();

                String sql = "DELETE FROM clientes WHERE id=?";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setInt(1, id);

                ps.executeUpdate();

                ps.close();
                con.close();

                exchange.getResponseHeaders().add("Location", "/clientes");

                exchange.sendResponseHeaders(302, -1);

                exchange.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

        });

        // ================= PRODUTOS =================

        server.createContext("/produtos", exchange -> {

            StringBuilder html = new StringBuilder();

            html.append("""
                    <html>

                    <head>

                    <meta charset="UTF-8">

                    <style>

                        table{
                            border-collapse:collapse;
                            width:100%;
                        }

                        th,td{
                            border:1px solid #ccc;
                            padding:8px;
                        }

                    </style>

                    </head>

                    <body>

                    <h2>Lista de Produtos</h2>

                    <a href='/novoProduto'>+ Novo Produto</a>

                    <br><br>

                    <table>

                    <tr>
                        <th>ID</th>
                        <th>RefProduto</th>
                        <th>Produto</th>
                        <th>Preço</th>
                        <th>Ações</th>
                    </tr>
                    """);

            try {

                Connection con = LigacaoBD.ligar();

                Statement st = con.createStatement();

                ResultSet rs = st.executeQuery("SELECT * FROM produtos");

                while (rs.next()) {

                    int id = rs.getInt("id");

                    String refProduto = rs.getString("refProduto");

                    String produto = rs.getString("produto");

                    String preco = rs.getString("preco");

                    html.append("<tr>");
                    html.append("<td>").append(id).append("</td>");
                    html.append("<td>").append(refProduto).append("</td>");
                    html.append("<td>").append(produto).append("</td>");
                    html.append("<td>").append(preco).append("</td>");
                    html.append("<td>");

                    html.append("<a href='/editarProduto?id=")
                            .append(id)
                            .append("'>Editar</a>");

                    html.append("<a href='/apagarProduto?id=")
                            .append(id)
                            .append("'>Apagar</a>");

                    html.append("</td>");

                    html.append("</tr>");
                }

                rs.close();
                st.close();
                con.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

            html.append("""
                    </table>

                    </body>
                    </html>
                    """);

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.toString().getBytes().length);
            exchange.getResponseBody().write(html.toString().getBytes());
            exchange.close();

        });

        // NOVO PRODUTO

        server.createContext("/novoProduto", exchange -> {

            String html = """
                    <html>

                    <head>
                    <meta charset="UTF-8">
                    </head>

                    <body>

                    <h2>Novo Produto</h2>

                    <form method='POST' action='/guardarProduto'>

                        Referência:
                        <input name='refProduto'><br><br>

                        Produto:
                        <input name='produto'><br><br>

                        Preço:
                        <input name='preco'><br><br>

                        <button type='submit'>Guardar</button>

                    </form>

                    </body>

                    </html>
                    """;

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.getBytes().length);
            exchange.getResponseBody().write(html.getBytes());
            exchange.close();

        });

        //  GUARDAR PRODUTO 

        server.createContext("/guardarProduto", exchange -> {

            try {

                String body = new String(exchange.getRequestBody().readAllBytes(), "UTF-8");

                String[] params = body.split("&");

                String refProduto = "";
                String produto = "";
                String preco = "";

                for (String p : params) {

                    String[] kv = p.split("=");

                    if (kv.length == 2) {

                        String key = kv[0];

                        String value = java.net.URLDecoder.decode(kv[1], "UTF-8");

                        switch (key) {

                            case "refProduto":
                                refProduto = value;
                                break;

                            case "produto":
                                produto = value;
                                break;

                            case "preco":
                                preco = value;
                                break;
                        }
                    }
                }

                Connection con = LigacaoBD.ligar();

                String sql = "INSERT INTO produtos(refProduto,produto,preco) VALUES(?,?,?)";

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, refProduto);
                ps.setString(2, produto);
                ps.setString(3, preco);

                ps.executeUpdate();

                ps.close();
                con.close();

                exchange.getResponseHeaders().add("Location", "/produtos");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();

            } catch (Exception e) {

                e.printStackTrace();
            }

        });

        server.start();

        System.out.println("Servidor iniciado em http://localhost:8080");

    }

}