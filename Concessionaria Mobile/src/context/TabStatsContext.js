import React, { createContext, useContext, useState, useCallback } from 'react';

/**
 * Contadores compartilhados entre as telas e a barra de abas,
 * para badges e subtítulos atualizarem sem mudar o visual.
 */
const TabStatsContext = createContext(null);

export function TabStatsProvider({ children }) {
  const [carrosDisponiveis, setCarrosDisponiveis] = useState(0);
  const [totalVendas, setTotalVendas] = useState(0);

  const atualizarCarros = useCallback((n) => setCarrosDisponiveis(n), []);
  const atualizarVendas = useCallback((n) => setTotalVendas(n), []);

  return (
    <TabStatsContext.Provider
      value={{ carrosDisponiveis, totalVendas, atualizarCarros, atualizarVendas }}
    >
      {children}
    </TabStatsContext.Provider>
  );
}

export function useTabStats() {
  return useContext(TabStatsContext);
}
