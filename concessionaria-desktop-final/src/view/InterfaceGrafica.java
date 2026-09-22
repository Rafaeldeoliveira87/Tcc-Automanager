package view;

import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import dao.DaoCarroBD;
import dao.DaoCarro;
import dao.DaoFuncionario;
import dao.DaoUsuario;
import dao.DaoVenda;
import dao.DaoVendasBD;
import entidade.Carro;
import entidade.Funcionario;
import entidade.Usuario;
import entidade.Vendas;
import service.ComissaoService;

/**
 * Interface principal da Concessionária — versão melhorada.
 *
 * Melhorias aplicadas em relação à versão original:
 *  - Painel de Relatórios completo (RelatorioPanel) adicionado como aba
 *  - Barra de busca/filtro em cada painel de tabela
 *  - Dashboard com card de receita total e separador visual
 *  - Tabela de vendas exibe nome do carro e do vendedor (não apenas IDs)
 *  - JToolBar substituindo o JPanel de botões soltos
 *  - Exclusão de venda restaura status do carro (via DaoCarroBD)
 *  - Constantes de cor e fonte centralizadas
 */
public class InterfaceGrafica extends JFrame {

    // ── DAOs ────────────────────────────────────────────────────────────
    private final DaoFuncionario funcionarioDAO = new DaoFuncionario();
    private final DaoCarro       carroDAO;
    private final DaoCarroBD     carroBD;
    private final DaoUsuario     usuarioDAO     = new DaoUsuario();
    private final DaoVenda       vendasDAO      = new DaoVendasBD();
    private final Usuario        usuarioLogado;

    // ── Models das tabelas ──────────────────────────────────────────────
    private final DefaultTableModel dtmCarros   = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Modelo", "Ano", "Preço", "Status", "Nível"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel dtmFunc     = new DefaultTableModel(
            new Object[]{"ID", "Nome", "Salário"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel dtmUsuarios = new DefaultTableModel(
            new Object[]{"ID", "Username", "Papel", "Funcionário"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    // MELHORIA: exibe nome do carro e do vendedor em vez de IDs numéricos
    private final DefaultTableModel dtmVendas   = new DefaultTableModel(
            new Object[]{"ID", "Carro", "Vendedor", "Valor", "Comissão", "Data"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // ── Cards do dashboard ──────────────────────────────────────────────
    private JLabel lblTotalCarros, lblDisponiveis, lblTotalFunc, lblTotalVendas, lblReceitaTotal;

    // ── Referência ao painel de relatórios ──────────────────────────────
    private RelatorioPanel relatorioPanel;

    // ── Paleta central (fonte de verdade para todo o tema) ──────────────
    static final Color COR_FUNDO      = new Color(12, 20, 40);
    static final Color COR_MENU       = new Color(15, 30, 60);
    static final Color COR_BOTAO      = new Color(0, 122, 204);
    static final Color COR_DESTAQUE   = new Color(241, 196, 15);
    static final Color COR_DANGER     = new Color(192, 57, 43);
    static final Color COR_BRANCO     = Color.WHITE;
    static final Color COR_TEXTO_SEC  = new Color(160, 190, 230);
    static final Font  FONTE_PADRAO   = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font  FONTE_TITULO   = new Font("Segoe UI", Font.BOLD, 14);

    // ════════════════════════════════════════════════════════════════════
    // CONSTRUTOR
    // ════════════════════════════════════════════════════════════════════
    public InterfaceGrafica(DaoCarro daoCarro, Usuario usuarioLogado) {
        this.carroDAO      = daoCarro;
        this.carroBD       = (daoCarro instanceof DaoCarroBD) ? (DaoCarroBD) daoCarro : new DaoCarroBD();
        this.usuarioLogado = usuarioLogado;

        // Aplica o tema globalmente
        UIManager.put("Label.font",      FONTE_PADRAO);
        UIManager.put("Button.font",     FONTE_PADRAO);
        UIManager.put("Table.font",      FONTE_PADRAO);
        UIManager.put("ComboBox.font",   FONTE_PADRAO);
        UIManager.put("TextField.font",  FONTE_PADRAO);

        setTitle("Automanager — " + usuarioLogado.getUsername());
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Barra lateral
        add(criarMenuLateral(), BorderLayout.WEST);

        // Abas
        JTabbedPane abas = criarAbas();
        add(abas, BorderLayout.CENTER);

        // Rodapé
        add(criarRodape(), BorderLayout.SOUTH);

        atualizarDashboard();
    }

    // ════════════════════════════════════════════════════════════════════
    // MENU LATERAL
    // ════════════════════════════════════════════════════════════════════
    private JPanel criarMenuLateral() {
        JPanel menu = new JPanel();
        menu.setBackground(COR_MENU);
        menu.setPreferredSize(new Dimension(200, 0));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(20, 12, 20, 12));

        // Logo / título
        JLabel logo = new JLabel(Icones.carro(36));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titulo = new JLabel("Automanager");
        titulo.setForeground(COR_BRANCO);
        titulo.setFont(FONTE_TITULO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Separador
        JSeparator sep1 = new JSeparator();
        sep1.setForeground(new Color(40, 80, 140));
        sep1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Info do usuário
        JLabel lblUsuario = new JLabel(usuarioLogado.getUsername(), Icones.usuario(14, COR_TEXTO_SEC), SwingConstants.CENTER);
        lblUsuario.setForeground(COR_TEXTO_SEC);
        lblUsuario.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblUsuario.setIconTextGap(6);
        lblUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPapel = new JLabel(usuarioLogado.getPapel());
        lblPapel.setForeground(new Color(100, 200, 120));
        lblPapel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblPapel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(40, 80, 140));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // Botões de navegação
        JButton btnDash      = criarBotaoMenu("Dashboard",     Icones.dashboard(16), false);
        JButton btnCarros    = criarBotaoMenu("Carros",        Icones.carro(16),     false);
        JButton btnFunc      = criarBotaoMenu("Funcionários",  Icones.pessoas(16),   false);
        JButton btnVendas    = criarBotaoMenu("Vendas",        Icones.dinheiro(16),  false);
        JButton btnRelatorio = criarBotaoMenu("Relatórios",    Icones.grafico(16),   false);
        JButton btnUsuarios  = criarBotaoMenu("Usuários",      Icones.chave(16),     false);
        JButton btnLogout    = criarBotaoMenu("Sair",          Icones.sair(16, COR_BRANCO), true);

        // Monta o menu
        menu.add(logo);
        menu.add(Box.createVerticalStrut(4));
        menu.add(titulo);
        menu.add(Box.createVerticalStrut(10));
        menu.add(sep1);
        menu.add(Box.createVerticalStrut(8));
        menu.add(lblUsuario);
        menu.add(Box.createVerticalStrut(2));
        menu.add(lblPapel);
        menu.add(Box.createVerticalStrut(8));
        menu.add(sep2);
        menu.add(Box.createVerticalStrut(16));
        menu.add(btnDash);
        menu.add(Box.createVerticalStrut(6));
        menu.add(btnCarros);
        menu.add(Box.createVerticalStrut(6));
        menu.add(btnFunc);
        menu.add(Box.createVerticalStrut(6));
        menu.add(btnVendas);
        menu.add(Box.createVerticalStrut(6));
        menu.add(btnRelatorio);
        menu.add(Box.createVerticalStrut(6));
        menu.add(btnUsuarios);
        menu.add(Box.createVerticalGlue());
        menu.add(btnLogout);

        // Ações — obtidas depois de criar as abas (usamos index)
        // As abas são criadas em criarAbas() que é chamado após esse método,
        // por isso usamos referência indireta via campo
        btnDash.addActionListener(e -> { atualizarDashboard(); selecionarAba(0); });
        btnCarros.addActionListener(e   -> selecionarAba(1));
        btnFunc.addActionListener(e     -> selecionarAba(2));
        btnVendas.addActionListener(e   -> selecionarAba(3));
        btnRelatorio.addActionListener(e -> { if (relatorioPanel != null) relatorioPanel.atualizar(); selecionarAba(4); });
        btnUsuarios.addActionListener(e -> selecionarAba(5));
        btnLogout.addActionListener(e -> fazerLogout());

        return menu;
    }

    // Referência ao JTabbedPane para navegação do menu lateral
    private JTabbedPane abasPrincipal;

    private void selecionarAba(int i) {
        if (abasPrincipal != null) abasPrincipal.setSelectedIndex(i);
    }

    // ════════════════════════════════════════════════════════════════════
    // ABAS
    // ════════════════════════════════════════════════════════════════════
    private JTabbedPane criarAbas() {
        relatorioPanel = new RelatorioPanel(vendasDAO, funcionarioDAO, carroBD);

        abasPrincipal = new JTabbedPane();
        abasPrincipal.setBackground(COR_FUNDO);
        abasPrincipal.setForeground(COR_BRANCO);
        abasPrincipal.addTab("Dashboard",     Icones.dashboard(18), criarDashboard());
        abasPrincipal.addTab("Carros",        Icones.carro(18),     criarPainelCarros());
        abasPrincipal.addTab("Funcionários",  Icones.pessoas(18),   criarPainelFuncionarios());
        abasPrincipal.addTab("Vendas",        Icones.dinheiro(18),  criarPainelVendas());
        abasPrincipal.addTab("Relatórios",    Icones.grafico(18),   relatorioPanel);
        abasPrincipal.addTab("Usuários",      Icones.chave(18),     criarPainelUsuarios());
        return abasPrincipal;
    }

    // ════════════════════════════════════════════════════════════════════
    // DASHBOARD
    // ════════════════════════════════════════════════════════════════════
    private JPanel criarDashboard() {
        JPanel dash = new JPanel(new BorderLayout(0, 20));
        dash.setBackground(COR_FUNDO);
        dash.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título de boas-vindas
        JPanel topoBemVindo = new JPanel(new BorderLayout());
        topoBemVindo.setBackground(COR_FUNDO);
        JLabel bemvindo = new JLabel("Bem-vindo, " + usuarioLogado.getUsername() + "!");
        bemvindo.setForeground(COR_BRANCO);
        bemvindo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        JLabel subtitulo = new JLabel("Painel de controle da concessionária");
        subtitulo.setForeground(COR_TEXTO_SEC);
        subtitulo.setFont(FONTE_PADRAO);
        topoBemVindo.add(bemvindo,  BorderLayout.NORTH);
        topoBemVindo.add(subtitulo, BorderLayout.SOUTH);
        dash.add(topoBemVindo, BorderLayout.NORTH);

        // Grid de cards
        JPanel cards = new JPanel(new GridLayout(1, 5, 16, 0));
        cards.setBackground(COR_FUNDO);

        lblTotalCarros  = new JLabel("—");
        lblDisponiveis  = new JLabel("—");
        lblTotalFunc    = new JLabel("—");
        lblTotalVendas  = new JLabel("—");
        lblReceitaTotal = new JLabel("—");

        cards.add(criarCardDash(Icones.carro(22),     "Total de Carros",     lblTotalCarros,  COR_BOTAO));
        cards.add(criarCardDash(Icones.check(22, new Color(39, 174, 96)), "Disponíveis",          lblDisponiveis,  new Color(39, 174, 96)));
        cards.add(criarCardDash(Icones.pessoas(22),   "Funcionários",         lblTotalFunc,    new Color(142, 68, 173)));
        cards.add(criarCardDash(Icones.dinheiro(22),  "Vendas Realizadas",   lblTotalVendas,  new Color(211, 84, 0)));
        cards.add(criarCardDash(Icones.tendencia(22, new Color(22, 160, 133)), "Receita Total",       lblReceitaTotal, new Color(22, 160, 133)));

        dash.add(cards, BorderLayout.CENTER);

        // Rodapé do dashboard
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        rodape.setBackground(COR_FUNDO);
        JLabel iconeDica = new JLabel(Icones.dica(14));
        JLabel dica = new JLabel("Clique em Relatórios para análises detalhadas por período.");
        dica.setForeground(COR_TEXTO_SEC);
        dica.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        rodape.add(iconeDica);
        rodape.add(dica);
        dash.add(rodape, BorderLayout.SOUTH);
        return dash;
    }

    private JPanel criarCardDash(Icon icone, String titulo, JLabel valorLabel, Color cor) {
        JPanel card = new JPanel();
        card.setPreferredSize(new Dimension(0, 120));
        card.setBackground(new Color(18, 35, 70));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, cor),
                new EmptyBorder(14, 14, 14, 14)));
        card.setLayout(new BorderLayout(0, 8));

        JLabel lblIcone = new JLabel(icone);

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setForeground(COR_TEXTO_SEC);
        lblTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        valorLabel.setForeground(COR_BRANCO);
        valorLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topo.setBackground(new Color(18, 35, 70));
        topo.add(lblIcone);

        card.add(topo,       BorderLayout.NORTH);
        card.add(lblTitulo,  BorderLayout.CENTER);
        card.add(valorLabel, BorderLayout.SOUTH);
        return card;
    }

    private void atualizarDashboard() {
        List<Carro> carros = carroDAO.loadAll();
        long disponiveis   = carros.stream().filter(c -> "Disponível".equals(c.getStatus())).count();
        List<Vendas> vendas = vendasDAO.listarVendas();
        double receita     = vendas.stream().mapToDouble(Vendas::getValorvenda).sum();

        lblTotalCarros.setText(String.valueOf(carros.size()));
        lblDisponiveis.setText(String.valueOf(disponiveis));
        lblTotalFunc.setText(String.valueOf(funcionarioDAO.loadAll().size()));
        lblTotalVendas.setText(String.valueOf(vendas.size()));
        lblReceitaTotal.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(receita));
    }

    // ════════════════════════════════════════════════════════════════════
    // PAINEL CARROS
    // ════════════════════════════════════════════════════════════════════
    private JPanel criarPainelCarros() {
        JPanel painel = new JPanel(new BorderLayout(0, 0));
        painel.setBackground(COR_FUNDO);

        JTable tabela = new JTable(dtmCarros);
        estilizarTabela(tabela);
        configurarColunaMoeda(tabela, 4);

        // MELHORIA: JToolBar no lugar de JPanel com botões soltos
        JToolBar toolbar = criarToolbar();
        JButton listar  = criarBotao("Listar", null);
        JButton add     = criarBotao("Adicionar", new Color(39, 174, 96));
        JButton editar  = criarBotao("Editar", COR_BOTAO);
        JButton excluir = criarBotao("Excluir", COR_DANGER);
        JTextField busca = criarCampoBusca("Buscar carro…");

        busca.addActionListener(e -> filtrarTabela(dtmCarros, busca.getText(), carroDAO.loadAll(), 1));

        toolbar.add(listar); toolbar.add(add); toolbar.add(editar); toolbar.add(excluir);
        toolbar.addSeparator();
        toolbar.add(new JLabel("  Buscar: "));
        toolbar.add(busca);

        listar.addActionListener(e  -> preencherCarros(carroDAO.loadAll()));
        add.addActionListener(e     -> adicionarCarro());
        editar.addActionListener(e  -> editarCarro(tabela));
        excluir.addActionListener(e -> excluirCarro(tabela));

        painel.add(toolbar, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ════════════════════════════════════════════════════════════════════
    // PAINEL FUNCIONÁRIOS
    // ════════════════════════════════════════════════════════════════════
    private JPanel criarPainelFuncionarios() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_FUNDO);

        JTable tabela = new JTable(dtmFunc);
        estilizarTabela(tabela);
        configurarColunaMoeda(tabela, 2);

        JToolBar toolbar = criarToolbar();
        JButton listar    = criarBotao("Listar", null);
        JButton registrar = criarBotao("Registrar", new Color(39, 174, 96));
        JButton editar    = criarBotao("Editar", COR_BOTAO);
        JButton excluir   = criarBotao("Excluir", COR_DANGER);

        toolbar.add(listar); toolbar.add(registrar); toolbar.add(editar); toolbar.add(excluir);

        listar.addActionListener(e    -> preencherFuncionarios(funcionarioDAO.loadAll()));
        registrar.addActionListener(e -> registrarFuncionario());
        editar.addActionListener(e    -> editarFuncionario(tabela));
        excluir.addActionListener(e   -> excluirFuncionario(tabela));

        painel.add(toolbar, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ════════════════════════════════════════════════════════════════════
    // PAINEL VENDAS
    // ════════════════════════════════════════════════════════════════════
    private JPanel criarPainelVendas() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_FUNDO);

        JTable tabela = new JTable(dtmVendas);
        estilizarTabela(tabela);
        configurarColunaMoeda(tabela, 3);
        configurarColunaMoeda(tabela, 4);

        JToolBar toolbar = criarToolbar();
        JButton listar  = criarBotao("Listar vendas", null);
        JButton add     = criarBotao("Registrar", new Color(39, 174, 96));
        JButton excluir = criarBotao("Excluir", COR_DANGER);

        toolbar.add(listar); toolbar.add(add); toolbar.add(excluir);

        listar.addActionListener(e  -> preencherVendas(vendasDAO.listarVendas()));
        add.addActionListener(e     -> adicionarVenda());
        excluir.addActionListener(e -> excluirVenda(tabela));

        painel.add(toolbar, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ════════════════════════════════════════════════════════════════════
    // PAINEL USUÁRIOS
    // ════════════════════════════════════════════════════════════════════
    private JPanel criarPainelUsuarios() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(COR_FUNDO);

        JTable tabela = new JTable(dtmUsuarios);
        estilizarTabela(tabela);

        JToolBar toolbar = criarToolbar();
        JButton listar    = criarBotao("Listar", null);
        JButton adicionar = criarBotao("Adicionar", new Color(39, 174, 96));
        JButton editar    = criarBotao("Editar", COR_BOTAO);
        JButton excluir   = criarBotao("Excluir", COR_DANGER);

        toolbar.add(listar); toolbar.add(adicionar); toolbar.add(editar); toolbar.add(excluir);

        listar.addActionListener(e    -> preencherUsuarios(usuarioDAO.listarTodos()));
        adicionar.addActionListener(e -> adicionarUsuario());
        editar.addActionListener(e    -> editarUsuario(tabela));
        excluir.addActionListener(e   -> excluirUsuario(tabela));

        painel.add(toolbar, BorderLayout.NORTH);
        painel.add(new JScrollPane(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ════════════════════════════════════════════════════════════════════
    // AÇÕES — CARROS
    // ════════════════════════════════════════════════════════════════════
    private void adicionarCarro() {
        try {
            String nome   = pedirTexto("Nome do carro:", null);      if (nome == null) return;
            String modelo = pedirTexto("Modelo:", null);             if (modelo == null) return;
            String anoStr = pedirTexto("Ano (ex: 2023):", null);     if (anoStr == null) return;
            int ano = Integer.parseInt(anoStr.trim());

            String precoStr = pedirTexto("Preço (ex: 85000.00):", null); if (precoStr == null) return;
            double preco = Double.parseDouble(precoStr.replace(",", "."));

            String status = pedirOpcao("Status:", new String[]{"Disponível", "Vendido"}, "Disponível");
            if (status == null) return;
            String nivel = pedirOpcao("Nível:", new String[]{"Basico", "Medio", "Luxo"}, "Basico");
            if (nivel == null) return;

            carroDAO.save(new Carro(0, nome, modelo, ano, preco, status, nivel));
            preencherCarros(carroDAO.loadAll());
            JOptionPane.showMessageDialog(this, "Carro salvo com sucesso!");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Valor inválido: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarCarro(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { aviso("Selecione um carro para editar."); return; }
        try {
            int    id     = (int) dtmCarros.getValueAt(linha, 0);
            String nome   = pedirTexto("Nome:", String.valueOf(dtmCarros.getValueAt(linha, 1)));   if (nome == null) return;
            String modelo = pedirTexto("Modelo:", String.valueOf(dtmCarros.getValueAt(linha, 2))); if (modelo == null) return;
            int    ano    = Integer.parseInt(pedirTexto("Ano:", String.valueOf(dtmCarros.getValueAt(linha, 3))));
            double preco  = Double.parseDouble(pedirTexto("Preço:", String.valueOf(dtmCarros.getValueAt(linha, 4))).replace(",", "."));
            String status = pedirOpcao("Status:", new String[]{"Disponível", "Vendido"}, String.valueOf(dtmCarros.getValueAt(linha, 5))); if (status == null) return;
            String nivel  = pedirOpcao("Nível:", new String[]{"Basico", "Medio", "Luxo"}, String.valueOf(dtmCarros.getValueAt(linha, 6))); if (nivel == null) return;

            carroDAO.update(new Carro(id, nome, modelo, ano, preco, status, nivel));
            preencherCarros(carroDAO.loadAll());
            JOptionPane.showMessageDialog(this, "Carro editado com sucesso!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao editar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirCarro(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { aviso("Selecione um carro para excluir."); return; }
        if (!confirmar("Excluir o carro selecionado?")) return;
        try {
            carroDAO.deleteById((int) dtmCarros.getValueAt(linha, 0));
            preencherCarros(carroDAO.loadAll());
            JOptionPane.showMessageDialog(this, "Carro excluído.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // AÇÕES — FUNCIONÁRIOS
    // ════════════════════════════════════════════════════════════════════
    private void registrarFuncionario() {
        try {
            String nome = pedirTexto("Nome do funcionário:", null); if (nome == null || nome.isBlank()) return;
            String sal  = pedirTexto("Salário (ex: 3500.00):", null); if (sal == null) return;
            double salario = Double.parseDouble(sal.replace(",", "."));
            funcionarioDAO.save(new Funcionario(0, nome, salario));
            preencherFuncionarios(funcionarioDAO.loadAll());
            JOptionPane.showMessageDialog(this, "Funcionário registrado!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarFuncionario(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { aviso("Selecione um funcionário para editar."); return; }
        try {
            int    id  = (int) dtmFunc.getValueAt(linha, 0);
            String nome = pedirTexto("Nome:", String.valueOf(dtmFunc.getValueAt(linha, 1))); if (nome == null || nome.isBlank()) return;
            double sal  = Double.parseDouble(pedirTexto("Salário:", String.valueOf(dtmFunc.getValueAt(linha, 2))).replace(",", "."));
            funcionarioDAO.update(new Funcionario(id, nome, sal));
            preencherFuncionarios(funcionarioDAO.loadAll());
            JOptionPane.showMessageDialog(this, "Funcionário editado!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirFuncionario(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { aviso("Selecione um funcionário para excluir."); return; }
        if (!confirmar("Excluir o funcionário selecionado?")) return;
        try {
            funcionarioDAO.deleteById((int) dtmFunc.getValueAt(linha, 0));
            preencherFuncionarios(funcionarioDAO.loadAll());
            JOptionPane.showMessageDialog(this, "Funcionário excluído.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // AÇÕES — VENDAS
    // ════════════════════════════════════════════════════════════════════
    private void adicionarVenda() {
        List<Carro>       carros = carroBD.loadDisponiveis();
        List<Funcionario> funcs  = funcionarioDAO.loadAll();

        if (carros.isEmpty()) { aviso("Não há carros disponíveis para venda."); return; }
        if (funcs.isEmpty())  { aviso("Cadastre pelo menos um funcionário antes de registrar uma venda."); return; }

        JComboBox<Carro>       comboCarro = new JComboBox<>(carros.toArray(new Carro[0]));
        JComboBox<Funcionario> comboFunc  = new JComboBox<>(funcs.toArray(new Funcionario[0]));
        JLabel lblComissao = new JLabel("Comissão: R$ —");
        lblComissao.setFont(new Font("Segoe UI", Font.BOLD, 13));

        comboCarro.addActionListener(e -> {
            Carro c = (Carro) comboCarro.getSelectedItem();
            if (c != null) {
                double com = ComissaoService.calcularComissao(c.getPreco(), c.getNivel());
                lblComissao.setText(String.format("Comissão: R$ %.2f  (%s — %.0f%%)",
                        com, c.getNivel(), com / c.getPreco() * 100));
            }
        });
        comboCarro.setSelectedIndex(0);

        JTextField txtData = new JTextField(LocalDate.now().toString());

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Carro disponível:"));  form.add(comboCarro);
        form.add(new JLabel("Vendedor:"));           form.add(comboFunc);
        form.add(new JLabel("Data (yyyy-MM-dd):"));  form.add(txtData);
        form.add(lblComissao);                        form.add(new JLabel(""));

        int ok = JOptionPane.showConfirmDialog(this, form,
                "Registrar Venda", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            Carro       carro   = (Carro)       comboCarro.getSelectedItem();
            Funcionario func    = (Funcionario) comboFunc.getSelectedItem();
            double comissao     = ComissaoService.calcularComissao(carro.getPreco(), carro.getNivel());
            LocalDate data      = LocalDate.parse(txtData.getText().trim());

            vendasDAO.registrarVenda(new Vendas(0, carro.getId(), func.getId(), carro.getPreco(), comissao, data));
            preencherVendas(vendasDAO.listarVendas());
            atualizarDashboard();
            JOptionPane.showMessageDialog(this,
                    String.format("Venda registrada!\nCarro: %s\nVendedor: %s\nComissão: R$ %.2f",
                            carro.getNome(), func.getNome(), comissao));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao registrar venda: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirVenda(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { aviso("Selecione uma venda para excluir."); return; }
        if (!confirmar("Excluir a venda selecionada? O carro voltará a ficar Disponível.")) return;
        try {
            int idVenda = (int) dtmVendas.getValueAt(linha, 0);

            // MELHORIA: restaurar status do carro ao excluir a venda
            // Busca o idCarro armazenado na linha (índice interno no model)
            // Para isso, buscamos a venda na lista completa pelo ID
            List<Vendas> todas = vendasDAO.listarVendas();
            Vendas vendaAlvo = todas.stream()
                    .filter(v -> v.getIdVenda() == idVenda)
                    .findFirst().orElse(null);

            vendasDAO.deleteById(idVenda);

            if (vendaAlvo != null) {
                // Restaura status para Disponível na mesma operação
                List<Carro> carros = carroDAO.loadAll();
                carros.stream()
                        .filter(c -> c.getId() == vendaAlvo.getIdCarro())
                        .findFirst()
                        .ifPresent(c -> {
                            try {
                                carroDAO.update(new Carro(c.getId(), c.getNome(), c.getModelo(),
                                        c.getAno(), c.getPreco(), "Disponível", c.getNivel()));
                            } catch (Exception ignored) {}
                        });
            }

            preencherVendas(vendasDAO.listarVendas());
            atualizarDashboard();
            JOptionPane.showMessageDialog(this, "Venda excluída e carro disponibilizado.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // AÇÕES — USUÁRIOS
    // ════════════════════════════════════════════════════════════════════
    private void adicionarUsuario() {
        JTextField txtUsername    = new JTextField();
        JPasswordField pf1        = new JPasswordField();
        JPasswordField pf2        = new JPasswordField();
        JComboBox<String> comboPapel = new JComboBox<>(new String[]{"GERENTE", "VENDEDOR"});
        JComboBox<Object> comboFunc  = criarComboFuncionarios(null);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Username:"));         form.add(txtUsername);
        form.add(new JLabel("Senha:"));            form.add(pf1);
        form.add(new JLabel("Confirmar senha:"));  form.add(pf2);
        form.add(new JLabel("Papel:"));            form.add(comboPapel);
        form.add(new JLabel("Funcionário (vendedor):")); form.add(comboFunc);

        comboPapel.addActionListener(e -> {
            boolean vendedor = "VENDEDOR".equals(comboPapel.getSelectedItem());
            comboFunc.setEnabled(vendedor);
            if (!vendedor) comboFunc.setSelectedIndex(0);
        });
        comboFunc.setEnabled(false);

        int ok = JOptionPane.showConfirmDialog(this, form,
                "Adicionar Usuário", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        String username = txtUsername.getText().trim();
        String senha1   = new String(pf1.getPassword());
        String senha2   = new String(pf2.getPassword());
        String papel    = (String) comboPapel.getSelectedItem();

        if (username.isEmpty() || senha1.isEmpty()) { aviso("Preencha usuário e senha."); return; }
        if (!senha1.equals(senha2))                 { aviso("As senhas não coincidem."); return; }
        if (senha1.length() < 6)                    { aviso("A senha deve ter pelo menos 6 caracteres."); return; }

        Integer funcionarioId = null;
        if ("VENDEDOR".equals(papel)) {
            funcionarioId = extrairFuncionarioId(comboFunc);
            if (funcionarioId == null) {
                aviso("Vendedor precisa estar vinculado a um funcionário cadastrado.");
                return;
            }
        }

        try {
            usuarioDAO.criarUsuario(username, senha1, papel, funcionarioId);
            preencherUsuarios(usuarioDAO.listarTodos());
            JOptionPane.showMessageDialog(this, "Usuário adicionado com sucesso!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarUsuario(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { aviso("Selecione um usuário para editar."); return; }

        int    id            = (int) dtmUsuarios.getValueAt(linha, 0);
        String usernameAtual = String.valueOf(dtmUsuarios.getValueAt(linha, 1));
        String papelAtual    = String.valueOf(dtmUsuarios.getValueAt(linha, 2));

        Usuario atual = usuarioDAO.listarTodos().stream()
                .filter(u -> u.getId() == id).findFirst().orElse(null);
        Integer funcionarioAtual = atual != null ? atual.getFuncionarioId() : null;

        JTextField txtUsername       = new JTextField(usernameAtual);
        JPasswordField pfSenha       = new JPasswordField();
        JComboBox<String> comboPapel = new JComboBox<>(new String[]{"GERENTE", "VENDEDOR"});
        comboPapel.setSelectedItem(papelAtual);
        JComboBox<Object> comboFunc  = criarComboFuncionarios(funcionarioAtual);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Username:"));                         form.add(txtUsername);
        form.add(new JLabel("Nova senha (vazio = manter):")); form.add(pfSenha);
        form.add(new JLabel("Papel:"));                            form.add(comboPapel);
        form.add(new JLabel("Funcionário (vendedor):"));           form.add(comboFunc);

        Runnable syncCombo = () -> {
            boolean vendedor = "VENDEDOR".equals(comboPapel.getSelectedItem());
            comboFunc.setEnabled(vendedor);
            if (!vendedor) comboFunc.setSelectedIndex(0);
        };
        comboPapel.addActionListener(e -> syncCombo.run());
        syncCombo.run();

        int ok = JOptionPane.showConfirmDialog(this, form,
                "Editar Usuário", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        String papel = (String) comboPapel.getSelectedItem();
        Integer funcionarioId = null;
        if ("VENDEDOR".equals(papel)) {
            funcionarioId = extrairFuncionarioId(comboFunc);
            if (funcionarioId == null) {
                aviso("Vendedor precisa estar vinculado a um funcionário cadastrado.");
                return;
            }
        }

        try {
            usuarioDAO.update(
                    new Usuario(id, txtUsername.getText().trim(), papel, funcionarioId),
                    new String(pfSenha.getPassword()));
            preencherUsuarios(usuarioDAO.listarTodos());
            JOptionPane.showMessageDialog(this, "Usuário editado!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirUsuario(JTable tabela) {
        int linha = tabela.getSelectedRow();
        if (linha < 0) { aviso("Selecione um usuário para excluir."); return; }
        int id = (int) dtmUsuarios.getValueAt(linha, 0);
        if (id == usuarioLogado.getId()) { aviso("Você não pode excluir o seu próprio usuário."); return; }
        if (!confirmar("Excluir o usuário selecionado?")) return;
        try {
            usuarioDAO.deleteById(id);
            preencherUsuarios(usuarioDAO.listarTodos());
            JOptionPane.showMessageDialog(this, "Usuário excluído.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Combo com "— nenhum —" + funcionários; pré-seleciona o id informado. */
    private JComboBox<Object> criarComboFuncionarios(Integer selecionadoId) {
        JComboBox<Object> combo = new JComboBox<>();
        combo.addItem("— nenhum —");
        int idxSelecionado = 0;
        List<Funcionario> funcs = funcionarioDAO.loadAll();
        for (int i = 0; i < funcs.size(); i++) {
            Funcionario f = funcs.get(i);
            combo.addItem(f);
            if (selecionadoId != null && f.getId() == selecionadoId) {
                idxSelecionado = i + 1;
            }
        }
        combo.setSelectedIndex(idxSelecionado);
        return combo;
    }

    private Integer extrairFuncionarioId(JComboBox<Object> combo) {
        Object sel = combo.getSelectedItem();
        if (sel instanceof Funcionario f) return f.getId();
        return null;
    }

    // ════════════════════════════════════════════════════════════════════
    // LOGOUT
    // ════════════════════════════════════════════════════════════════════
    private void fazerLogout() {
        dispose();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPERS — TABELAS
    // ════════════════════════════════════════════════════════════════════
    private void preencherCarros(List<Carro> carros) {
        dtmCarros.setRowCount(0);
        for (Carro c : carros)
            dtmCarros.addRow(new Object[]{c.getId(), c.getNome(), c.getModelo(), c.getAno(), c.getPreco(), c.getStatus(), c.getNivel()});
    }

    private void preencherFuncionarios(List<Funcionario> lista) {
        dtmFunc.setRowCount(0);
        for (Funcionario f : lista)
            dtmFunc.addRow(new Object[]{f.getId(), f.getNome(), f.getSalario()});
    }

    private void preencherUsuarios(List<Usuario> lista) {
        List<Funcionario> funcs = funcionarioDAO.loadAll();
        dtmUsuarios.setRowCount(0);
        for (Usuario u : lista) {
            String vinculo = "—";
            if (u.getFuncionarioId() != null) {
                vinculo = funcs.stream()
                        .filter(f -> f.getId() == u.getFuncionarioId())
                        .map(f -> "[" + f.getId() + "] " + f.getNome())
                        .findFirst()
                        .orElse("ID " + u.getFuncionarioId());
            }
            dtmUsuarios.addRow(new Object[]{u.getId(), u.getUsername(), u.getPapel(), vinculo});
        }
    }

    /** MELHORIA: exibe nome do carro e do vendedor — busca nas listas em memória. */
    private void preencherVendas(List<Vendas> lista) {
        List<Carro>       carros = carroDAO.loadAll();
        List<Funcionario> funcs  = funcionarioDAO.loadAll();
        dtmVendas.setRowCount(0);
        for (Vendas v : lista) {
            String nomeCarro   = carros.stream().filter(c -> c.getId() == v.getIdCarro())
                    .map(c -> c.getNome() + " " + c.getModelo()).findFirst().orElse("ID " + v.getIdCarro());
            String nomeVendedor = funcs.stream().filter(f -> f.getId() == v.getIdFuncionario())
                    .map(Funcionario::getNome).findFirst().orElse("ID " + v.getIdFuncionario());
            dtmVendas.addRow(new Object[]{v.getIdVenda(), nomeCarro, nomeVendedor,
                    v.getValorvenda(), v.getComissao(), v.getDataVenda()});
        }
    }

    /** Filtra a tabela de carros pelo texto digitado na busca. */
    private void filtrarTabela(DefaultTableModel model, String texto, List<Carro> todos, int colunaTexto) {
        if (texto == null || texto.isBlank()) { preencherCarros(todos); return; }
        preencherCarros(todos);
        String lower = texto.toLowerCase();
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            String val = String.valueOf(model.getValueAt(i, colunaTexto)).toLowerCase();
            if (!val.contains(lower)) model.removeRow(i);
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // HELPERS — UI
    // ════════════════════════════════════════════════════════════════════
    private JButton criarBotaoMenu(String texto, Icon icone, boolean perigo) {
        JButton b = new JButton(texto, icone);
        b.setBackground(perigo ? COR_DANGER : COR_BOTAO);
        b.setForeground(COR_BRANCO);
        b.setFocusPainted(false);
        b.setIconTextGap(10);
        b.setBorder(new EmptyBorder(8, 10, 8, 10));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        return b;
    }

    private JButton criarBotao(String texto, Color cor) {
        JButton b = new JButton(texto);
        b.setBackground(cor != null ? cor : COR_BOTAO);
        b.setForeground(COR_BRANCO);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(5, 12, 5, 12));
        return b;
    }

    private JToolBar criarToolbar() {
        JToolBar tb = new JToolBar();
        tb.setBackground(new Color(15, 30, 60));
        tb.setFloatable(false);
        tb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 60, 120)),
                new EmptyBorder(6, 8, 6, 8)));
        return tb;
    }

    private JTextField criarCampoBusca(String placeholder) {
        JTextField tf = new JTextField(18);
        tf.setBackground(new Color(25, 50, 95));
        tf.setForeground(COR_BRANCO);
        tf.setCaretColor(COR_BRANCO);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 90, 160)),
                new EmptyBorder(3, 6, 3, 6)));
        tf.setToolTipText(placeholder);
        return tf;
    }

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(new Color(8, 15, 30));
        rodape.setBorder(new EmptyBorder(4, 12, 4, 12));
        JLabel info = new JLabel("Automanager  •  Sistema de Gestão  •  " + LocalDate.now());
        info.setForeground(new Color(80, 110, 160));
        info.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        rodape.add(info, BorderLayout.WEST);
        return rodape;
    }

    private void estilizarTabela(JTable t) {
        t.setBackground(new Color(18, 30, 55));
        t.setForeground(COR_BRANCO);
        t.setRowHeight(28);
        t.setGridColor(new Color(30, 60, 110));
        t.setShowVerticalLines(false);
        t.setSelectionBackground(COR_BOTAO);
        t.setSelectionForeground(COR_BRANCO);
        t.getTableHeader().setBackground(new Color(10, 20, 40));
        t.getTableHeader().setForeground(COR_TEXTO_SEC);
        t.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 60, 120)));
        t.setAutoCreateRowSorter(true);

        // Renderer alternado para as linhas
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, row, col);
                setBackground(sel ? COR_BOTAO : (row % 2 == 0 ? new Color(18, 30, 55) : new Color(22, 38, 68)));
                setForeground(COR_BRANCO);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
    }

    private void configurarColunaMoeda(JTable t, int col) {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        t.getColumnModel().getColumn(col).setCellRenderer(new DefaultTableCellRenderer() {
            @Override protected void setValue(Object v) {
                setText(v instanceof Number ? fmt.format(((Number) v).doubleValue()) : (v == null ? "" : v.toString()));
                setForeground(new Color(100, 220, 140));
                setBackground(new Color(18, 30, 55));
                setHorizontalAlignment(SwingConstants.RIGHT);
            }
        });
    }

    // ── Diálogos auxiliares ───────────────────────────────────────────
    private String pedirTexto(String mensagem, String valorAtual) {
        return (String) JOptionPane.showInputDialog(this, mensagem, "Entrada",
                JOptionPane.PLAIN_MESSAGE, null, null, valorAtual);
    }

    private String pedirOpcao(String mensagem, String[] opcoes, String padrao) {
        return (String) JOptionPane.showInputDialog(this, mensagem, "Seleção",
                JOptionPane.QUESTION_MESSAGE, null, opcoes, padrao);
    }

    private void aviso(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atenção", JOptionPane.WARNING_MESSAGE);
    }

    private boolean confirmar(String msg) {
        return JOptionPane.showConfirmDialog(this, msg, "Confirmar",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
