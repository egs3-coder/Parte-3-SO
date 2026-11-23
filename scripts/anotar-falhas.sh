#!/usr/bin/env bash
# Transforma as linhas [ERROR] do Maven em anotações do GitHub Actions,
# para o motivo da falha aparecer direto no pull request (aba "Files changed" e resumo do check).
# Uso: anotar-falhas.sh <arquivo-de-log>
set -uo pipefail
LOG="${1:?informe o log do maven}"
[ -f "$LOG" ] || exit 0

grep -E '^\[ERROR\]' "$LOG" \
  | grep -vE 'To see the full stack trace|Re-run Maven|For more information|Help 1|^\[ERROR\] *$|^\[ERROR\] -> ' \
  | head -60 > /tmp/erros.txt

# O GitHub mostra no máximo 10 anotações de erro por passo: agrupamos 6 linhas por anotação.
split -l 6 /tmp/erros.txt /tmp/erro-parte-
for parte in /tmp/erro-parte-*; do
  [ -f "$parte" ] || continue
  mensagem=$(sed 's/%/%25/g' "$parte" | awk 'BEGIN{ORS="%0A"} {print}')
  echo "::error title=Falha no build/testes::${mensagem}"
done
