#!/usr/bin/env bash
# Teste de fumaça depois do deploy: a versão nova responde e os contextos principais estão de pé.
# Uso: smoke-test.sh <url-base> <versao-esperada>
set -euo pipefail

URL="${1:?informe a URL base}"
VERSAO="${2:?informe a versão esperada}"
URL="${URL%/}"

checar() {
  local descricao="$1" caminho="$2" esperado="$3"
  local corpo
  corpo=$(curl -fsS --max-time 30 "${URL}${caminho}")
  if ! grep -q -- "$esperado" <<<"$corpo"; then
    echo "FALHOU: ${descricao} (${caminho}) não contém ${esperado}" >&2
    echo "$corpo" | head -c 500 >&2
    exit 1
  fi
  echo "ok  ${descricao}"
}

checar "versão publicada"            "/api/info"         "\"versao\":\"${VERSAO}\""
checar "saúde da aplicação e banco"  "/actuator/health"  "\"status\":\"UP\""
checar "Estoque responde"            "/estoque"          "O-"
checar "Rede responde"               "/rede/unidades"    "["
checar "Requisição responde"         "/requisicoes"      "["
echo "Teste de fumaça passou: versão ${VERSAO} no ar em ${URL}"
