Mini WMS - Frontend

Este frontend mínimo utiliza Vite + React e fornece uma interface simples para listar e criar produtos.

Como executar (local):

1. Instale Node (v18+ recomendado) e npm.
2. No diretório `frontend`, rode:

```bash
npm install
npm run dev
```

3. Abra `http://localhost:5173` (ou porta indicada pelo Vite).

Observações:
- O frontend consome a API em `/api/...` — ao rodar localmente junto com o backend, assegure que o backend esteja em `http://localhost:8080` ou configure proxy se necessário.
- O código é propositalmente simples para facilitar demonstração acadêmica; recomenda-se adicionar tratamento de erros, autenticação e componentes para as demais entidades (endereços, lotes, ordens, movimentações).
