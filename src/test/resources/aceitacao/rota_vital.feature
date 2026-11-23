# language: pt
Funcionalidade: Atender uma requisição de ponta a ponta (todos os contextos pela API)
  Como hemocentro
  Quero levar a bolsa certa do doador até o hospital, com a cadeia fria registrada
  Para que o paciente receba sangue compatível, válido e bem conservado

  # Passa pelos seis contextos: Rede, Captação (evento DoacaoRegistrada), Estoque, Requisição (agregado),
  # Expedição (comando MontarRemessa), Transporte (camada anticorrupção e evento EntregaConcluida).

  Contexto:
    Dado a unidade "HSA" "Hospital Sintético A" de "Recife"
    E um doador "Carla" do sexo "FEMININO" com 30 anos, 60 kg e tipo "O-"
    E "Carla" doa sangue hoje
    E o estoque recebe a bolsa de plasma "P-100" do tipo "AB-" coletada hoje

  Cenário: Requisição atendida, transportada e entregue
    Dado a requisição de "URGENCIA" da unidade "HSA" com os itens:
      | hemocomponente          | receptor | quantidade |
      | CONCENTRADO_HEMACIAS    | A+       | 1          |
      | PLASMA_FRESCO_CONGELADO | A+       | 1          |
    Quando a atendente aloca a bolsa doada por "Carla" ao item "ITEM-1"
    E a atendente aloca a bolsa "P-100" ao item "ITEM-2"
    E a atendente marca a requisição como atendida
    Então a requisição fica com status "ATENDIDA"
    E existe a remessa da requisição com 2 bolsas
    Quando a atendente despacha a requisição
    E o sensor da caixa envia 41 °F
    E o sensor da caixa envia 50 °F
    Então a viagem registrou 2 leituras e 1 excursão de temperatura
    Quando o motorista confirma a entrega
    Então a requisição fica com status "ENTREGUE"

  Cenário: O agregado recusa bolsa incompatível e o Estoque não fica com reserva presa
    Dado a requisição de "ROTINA" da unidade "HSA" com os itens:
      | hemocomponente          | receptor | quantidade |
      | PLASMA_FRESCO_CONGELADO | A+       | 1          |
    Quando a atendente aloca a bolsa doada por "Carla" ao item "ITEM-1"
    Então a API recusa com 422 e a mensagem "Hemocomponente diferente do pedido"
    E a bolsa doada por "Carla" continua "DISPONIVEL" no estoque

  Cenário: Requisição de emergência sem justificativa clínica é recusada
    Quando o hospital pede uma requisição de "EMERGENCIA" da unidade "HSA" sem justificativa
    Então a API recusa com 422 e a mensagem "Requisição de emergência exige justificativa clínica"

  Cenário: Cancelar devolve as bolsas ao estoque
    Dado a requisição de "ROTINA" da unidade "HSA" com os itens:
      | hemocomponente       | receptor | quantidade |
      | CONCENTRADO_HEMACIAS | O-       | 1          |
    Quando a atendente aloca a bolsa doada por "Carla" ao item "ITEM-1"
    E a atendente cancela a requisição com o motivo "Paciente transferido"
    Então a requisição fica com status "CANCELADA"
    E a bolsa doada por "Carla" continua "DISPONIVEL" no estoque
