import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, FlatList, StyleSheet, RefreshControl } from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useAuth } from '../context/AuthContext';
import { useTabStats } from '../context/TabStatsContext';
import { getMinhasVendas } from '../api';
import { colors, radius, spacing, cardShadow } from '../theme';
import StatCard from '../components/StatCard';
import BarChart from '../components/BarChart';

const MESES = ['Jan', 'Fev', 'Mar', 'Abr', 'Mai', 'Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov', 'Dez'];

/** Agrupa as vendas por mês (últimos até 6 meses com dados) para o gráfico. */
function agruparPorMes(vendas) {
  const totais = {}; // "2026-08" -> soma da comissão
  for (const v of vendas) {
    if (!v.dataVenda) continue;
    const chave = String(v.dataVenda).slice(0, 7); // "2026-08"
    totais[chave] = (totais[chave] || 0) + v.comissao;
  }
  return Object.keys(totais)
    .sort()
    .slice(-6)
    .map((chave) => {
      const mesIndex = parseInt(chave.slice(5, 7), 10) - 1;
      return { label: MESES[mesIndex] ?? chave, value: totais[chave] };
    });
}

export default function MinhasVendasScreen({ navigation }) {
  const { sessao } = useAuth();
  const { atualizarVendas } = useTabStats();
  const [vendas, setVendas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [erro, setErro] = useState(null);

  const carregar = useCallback(async () => {
    try {
      const data = await getMinhasVendas(sessao.token);
      setVendas(data);
      atualizarVendas(data.length);
      setErro(null);
    } catch (e) {
      setErro(e.message);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [sessao.token, atualizarVendas]);

  React.useEffect(() => {
    const unsubscribe = navigation.addListener('focus', carregar);
    return unsubscribe;
  }, [navigation, carregar]);

  const totalComissao = useMemo(() => vendas.reduce((s, v) => s + v.comissao, 0), [vendas]);
  const ticketMedio = vendas.length > 0 ? vendas.reduce((s, v) => s + v.valorvenda, 0) / vendas.length : 0;
  const dadosGrafico = useMemo(() => agruparPorMes(vendas), [vendas]);

  if (loading && vendas.length === 0) {
    return (
      <View style={[styles.container, styles.centered]}>
        <Text style={styles.loading}>Carregando...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <FlatList
        data={vendas}
        keyExtractor={(item) => String(item.idVenda)}
        contentContainerStyle={{ paddingBottom: spacing.xl }}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={() => { setRefreshing(true); carregar(); }}
            tintColor={colors.accent}
            colors={[colors.accent]}
          />
        }
        ListHeaderComponent={
          <View>
            <Text style={styles.headerTitulo}>Minhas vendas</Text>

            <View style={styles.statsRow}>
              <StatCard label="Vendas" valor={String(vendas.length)} />
              <StatCard label="Comissão total" valor={`R$ ${totalComissao.toFixed(0)}`} destaque />
              <StatCard label="Ticket médio" valor={`R$ ${ticketMedio.toFixed(0)}`} />
            </View>

            {dadosGrafico.length > 0 && (
              <View style={styles.graficoCard}>
                <Text style={styles.graficoTitulo}>Comissão por mês</Text>
                <BarChart data={dadosGrafico} />
              </View>
            )}

            {vendas.length > 0 && <Text style={styles.listaTitulo}>Histórico</Text>}
          </View>
        }
        ListEmptyComponent={
          <View style={styles.vazio}>
            <Ionicons name="receipt-outline" size={40} color={colors.textMuted} />
            <Text style={styles.empty}>Você ainda não registrou nenhuma venda.</Text>
          </View>
        }
        renderItem={({ item }) => (
          <View style={styles.card}>
            <View style={styles.cardIcone}>
              <Ionicons name="cash-outline" size={18} color={colors.gold} />
            </View>
            <View style={{ flex: 1 }}>
              <Text style={styles.data}>{item.dataVenda}</Text>
              <Text style={styles.detalhe}>Carro #{item.idCarro} · R$ {Number(item.valorvenda).toFixed(2)}</Text>
            </View>
            <Text style={styles.comissao}>+R$ {Number(item.comissao).toFixed(2)}</Text>
          </View>
        )}
      />
      {erro ? <Text style={styles.erro}>{erro}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: spacing.md, backgroundColor: colors.bgPrimary },
  centered: { justifyContent: 'center', alignItems: 'center' },
  loading: { color: colors.textSecondary },
  headerTitulo: { color: colors.textPrimary, fontSize: 20, fontWeight: '800', marginBottom: spacing.md },
  statsRow: { flexDirection: 'row', gap: spacing.sm, marginBottom: spacing.md },
  graficoCard: {
    backgroundColor: colors.bgCard,
    borderRadius: radius.lg,
    padding: spacing.md,
    marginBottom: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
    ...cardShadow,
  },
  graficoTitulo: { color: colors.textSecondary, fontSize: 13, fontWeight: '700' },
  listaTitulo: { color: colors.textSecondary, fontSize: 13, fontWeight: '700', marginBottom: spacing.sm },
  erro: { color: '#FF8A8A', marginBottom: spacing.sm },
  vazio: { alignItems: 'center', marginTop: spacing.xl, gap: spacing.sm },
  empty: { color: colors.textMuted, textAlign: 'center' },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
    backgroundColor: colors.bgCard,
    padding: spacing.md,
    borderRadius: radius.md,
    marginBottom: spacing.sm,
    borderWidth: 1,
    borderColor: colors.border,
  },
  cardIcone: {
    width: 36, height: 36, borderRadius: radius.md,
    backgroundColor: colors.bgInput, alignItems: 'center', justifyContent: 'center',
  },
  data: { color: colors.textMuted, fontSize: 11 },
  detalhe: { color: colors.textPrimary, marginTop: 2, fontWeight: '600' },
  comissao: { color: colors.success, fontWeight: '800' },
});
