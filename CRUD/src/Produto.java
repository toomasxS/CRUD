public class Produto {
   
    public int idP;
    public int refProduto;
    public String produto;
    public int preco;

    public Produto(int idP, int refProduto, String produto, int preco) {
        this.idP = idP;
        this.refProduto = refProduto;
        this.produto = produto;
        this.preco = preco;
    }


    @Override
    public String toString() {
        return produto;
    }
}
