package view;

import javax.swing.Icon;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Ícones desenhados via Java2D, usados no lugar de emoji.
 *
 * Por quê: emoji embutido em texto (ex: "🚗 Carros") depende de qual fonte
 * de emoji está instalada e configurada no Windows do usuário -- o Swing
 * não faz fallback automático de fonte por caractere como um navegador
 * faz, então o resultado varia de máquina pra máquina (às vezes aparecem
 * caixinhas vazias ou símbolos estranhos). Ícones desenhados na mão
 * sempre têm a mesma aparência, em qualquer computador.
 *
 * Uso: Icones.carro(18) devolve um Icon pronto pra usar em
 * JTabbedPane.addTab(titulo, icone, painel) ou em JLabel.setIcon(...).
 */
public final class Icones {

    private Icones() {}

    // ── Ícones de abas / navegação ──────────────────────────────────────

    public static Icon dashboard(int tamanho) {
        return icon(tamanho, (g, s) -> {
            g.setColor(new Color(0, 122, 204));
            int[] xs = {s / 2, s - 2, s - 2, 2, 2};
            int[] ys = {1, s / 2 - 1, s - 1, s - 1, s / 2 - 1};
            g.fillPolygon(xs, ys, 5);
            g.setColor(new Color(12, 20, 40));
            g.fillRect(s / 2 - 2, s / 2 + 1, 4, s / 2 - 3);
        });
    }

    public static Icon carro(int tamanho) {
        return icon(tamanho, (g, s) -> {
            g.setColor(new Color(0, 122, 204));
            g.fill(new RoundRectangle2D.Float(1, s * 0.42f, s - 2, s * 0.32f, 4, 4));
            int[] xs = {(int) (s * 0.2f), (int) (s * 0.32f), (int) (s * 0.68f), (int) (s * 0.8f)};
            int[] ys = {(int) (s * 0.42f), (int) (s * 0.18f), (int) (s * 0.18f), (int) (s * 0.42f)};
            g.fillPolygon(xs, ys, 4);
            g.setColor(new Color(12, 20, 40));
            g.fill(new Ellipse2D.Float(s * 0.12f, s * 0.66f, s * 0.24f, s * 0.24f));
            g.fill(new Ellipse2D.Float(s * 0.64f, s * 0.66f, s * 0.24f, s * 0.24f));
        });
    }

    public static Icon pessoas(int tamanho) {
        return icon(tamanho, (g, s) -> {
            g.setColor(new Color(142, 68, 173));
            g.fill(new Ellipse2D.Float(s * 0.08f, s * 0.12f, s * 0.4f, s * 0.4f));
            g.fill(new RoundRectangle2D.Float(0, s * 0.55f, s * 0.56f, s * 0.4f, 6, 6));
            g.setColor(new Color(180, 130, 210));
            g.fill(new Ellipse2D.Float(s * 0.5f, s * 0.05f, s * 0.42f, s * 0.42f));
            g.fill(new RoundRectangle2D.Float(s * 0.42f, s * 0.5f, s * 0.58f, s * 0.44f, 6, 6));
        });
    }

    public static Icon dinheiro(int tamanho) {
        return icon(tamanho, (g, s) -> {
            g.setColor(new Color(39, 174, 96));
            g.fill(new RoundRectangle2D.Float(0, s * 0.2f, s, s * 0.6f, 6, 6));
            g.setColor(new Color(12, 20, 40));
            g.fill(new Ellipse2D.Float(s * 0.36f, s * 0.34f, s * 0.28f, s * 0.32f));
            g.setColor(new Color(39, 174, 96));
            g.fillOval((int) (s * 0.42f), (int) (s * 0.4f), (int) (s * 0.16f), (int) (s * 0.2f));
        });
    }

    public static Icon grafico(int tamanho) {
        return icon(tamanho, (g, s) -> {
            g.setColor(new Color(230, 126, 34));
            g.fillRect((int) (s * 0.08f), (int) (s * 0.55f), (int) (s * 0.2f), (int) (s * 0.4f));
            g.setColor(new Color(0, 122, 204));
            g.fillRect((int) (s * 0.4f), (int) (s * 0.3f), (int) (s * 0.2f), (int) (s * 0.65f));
            g.setColor(new Color(39, 174, 96));
            g.fillRect((int) (s * 0.72f), (int) (s * 0.1f), (int) (s * 0.2f), (int) (s * 0.85f));
        });
    }

    public static Icon chave(int tamanho) {
        return icon(tamanho, (g, s) -> {
            g.setColor(new Color(241, 196, 15));
            g.setStroke(new BasicStroke(Math.max(2f, s * 0.16f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new Ellipse2D.Float(s * 0.05f, s * 0.05f, s * 0.5f, s * 0.5f));
            g.drawLine((int) (s * 0.45f), (int) (s * 0.45f), (int) (s * 0.92f), (int) (s * 0.92f));
            g.drawLine((int) (s * 0.68f), (int) (s * 0.68f), (int) (s * 0.8f), (int) (s * 0.56f));
        });
    }

    public static Icon check(int tamanho, Color cor) {
        return icon(tamanho, (g, s) -> {
            g.setColor(cor);
            g.setStroke(new BasicStroke(Math.max(2f, s * 0.16f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine((int) (s * 0.15f), (int) (s * 0.5f), (int) (s * 0.4f), (int) (s * 0.75f));
            g.drawLine((int) (s * 0.4f), (int) (s * 0.75f), (int) (s * 0.88f), (int) (s * 0.2f));
        });
    }

    public static Icon tendencia(int tamanho, Color cor) {
        return icon(tamanho, (g, s) -> {
            g.setColor(cor);
            g.setStroke(new BasicStroke(Math.max(2f, s * 0.14f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine((int) (s * 0.05f), (int) (s * 0.8f), (int) (s * 0.4f), (int) (s * 0.45f));
            g.drawLine((int) (s * 0.4f), (int) (s * 0.45f), (int) (s * 0.6f), (int) (s * 0.65f));
            g.drawLine((int) (s * 0.6f), (int) (s * 0.65f), (int) (s * 0.95f), (int) (s * 0.15f));
            int[] xs = {(int) (s * 0.95f), (int) (s * 0.7f), (int) (s * 0.95f)};
            int[] ys = {(int) (s * 0.15f), (int) (s * 0.15f), (int) (s * 0.4f)};
            g.fillPolygon(xs, ys, 3);
        });
    }

    public static Icon dica(int tamanho) {
        return icon(tamanho, (g, s) -> {
            g.setColor(new Color(241, 196, 15));
            g.fill(new Ellipse2D.Float(s * 0.2f, s * 0.05f, s * 0.6f, s * 0.6f));
            g.fillRect((int) (s * 0.36f), (int) (s * 0.62f), (int) (s * 0.28f), (int) (s * 0.22f));
            g.setColor(new Color(12, 20, 40));
            g.fillRect((int) (s * 0.36f), (int) (s * 0.86f), (int) (s * 0.28f), (int) (s * 0.08f));
        });
    }

    /** Medalha de ranking -- ouro/prata/bronze conforme a posição (1, 2, 3...). */
    public static Icon medalha(int tamanho, int posicao) {
        Color cor = switch (posicao) {
            case 1 -> new Color(241, 196, 15);
            case 2 -> new Color(189, 195, 199);
            case 3 -> new Color(205, 127, 50);
            default -> new Color(90, 110, 140);
        };
        return icon(tamanho, (g, s) -> {
            g.setColor(cor);
            g.fill(new Ellipse2D.Float(s * 0.1f, s * 0.15f, s * 0.8f, s * 0.8f));
            g.setColor(new Color(12, 20, 40));
            g.setFont(new Font("Segoe UI", Font.BOLD, Math.round(s * 0.42f)));
            String texto = String.valueOf(posicao);
            FontMetrics fm = g.getFontMetrics();
            int tx = (s - fm.stringWidth(texto)) / 2;
            int ty = (int) (s * 0.15f) + (int) (s * 0.8f) / 2 + fm.getAscent() / 2 - 2;
            g.drawString(texto, tx, ty);
        });
    }

    public static Icon atualizar(int tamanho, Color cor) {
        return icon(tamanho, (g, s) -> {
            g.setColor(cor);
            g.setStroke(new BasicStroke(Math.max(2f, s * 0.14f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawArc((int) (s * 0.1f), (int) (s * 0.1f), (int) (s * 0.8f), (int) (s * 0.8f), 40, 270);
            int[] xs = {(int) (s * 0.78f), (int) (s * 0.95f), (int) (s * 0.65f)};
            int[] ys = {(int) (s * 0.05f), (int) (s * 0.22f), (int) (s * 0.28f)};
            g.fillPolygon(xs, ys, 3);
        });
    }

    public static Icon exportar(int tamanho, Color cor) {
        return icon(tamanho, (g, s) -> {
            g.setColor(cor);
            g.setStroke(new BasicStroke(Math.max(2f, s * 0.14f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(s / 2, (int) (s * 0.1f), s / 2, (int) (s * 0.6f));
            int[] xs = {(int) (s * 0.28f), s / 2, (int) (s * 0.72f)};
            int[] ys = {(int) (s * 0.42f), (int) (s * 0.68f), (int) (s * 0.42f)};
            g.fillPolygon(xs, ys, 3);
            g.drawLine((int) (s * 0.12f), (int) (s * 0.88f), (int) (s * 0.88f), (int) (s * 0.88f));
        });
    }

    public static Icon usuario(int tamanho, Color cor) {
        return icon(tamanho, (g, s) -> {
            g.setColor(cor);
            g.fill(new Ellipse2D.Float(s * 0.28f, s * 0.05f, s * 0.44f, s * 0.44f));
            g.fill(new RoundRectangle2D.Float(s * 0.1f, s * 0.55f, s * 0.8f, s * 0.4f, 6, 6));
        });
    }

    public static Icon sair(int tamanho, Color cor) {
        return icon(tamanho, (g, s) -> {
            g.setColor(cor);
            g.setStroke(new BasicStroke(Math.max(2f, s * 0.14f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(new RoundRectangle2D.Float(s * 0.08f, s * 0.1f, s * 0.42f, s * 0.8f, 4, 4));
            g.drawLine((int) (s * 0.4f), (int) (s * 0.5f), (int) (s * 0.95f), (int) (s * 0.5f));
            int[] xs = {(int) (s * 0.75f), (int) (s * 0.95f), (int) (s * 0.75f)};
            int[] ys = {(int) (s * 0.32f), (int) (s * 0.5f), (int) (s * 0.68f)};
            g.fillPolygon(xs, ys, 3);
        });
    }

    // ── Infraestrutura interna ──────────────────────────────────────────

    private interface Desenho { void desenhar(Graphics2D g, int s); }

    private static Icon icon(int tamanho, Desenho desenho) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                desenho.desenhar(g2, tamanho);
                g2.dispose();
            }

            @Override public int getIconWidth()  { return tamanho; }
            @Override public int getIconHeight() { return tamanho; }
        };
    }
}
