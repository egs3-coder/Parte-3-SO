# Especificações (SDD, opção 3 do enunciado: Markdown manual)

Cada funcionalidade tem um `spec.md` escrito **antes** do código que ele gera; o histórico de commits mostra a
ordem (a spec entra num commit `docs(sdd)` anterior aos commits de teste e de código). Os termos seguem o
glossário da Parte 2 ([`docs/arquitetura/documento.md`](../docs/arquitetura/documento.md#a3-glossário-da-linguagem-ubíqua)).

| Contexto | Spec | Código que ela guiou |
| --- | --- | --- |
| Requisição (core) | [`requisicao/alocar-bolsa.md`](requisicao/alocar-bolsa.md) | agregado `Requisicao`, caso de uso `AlocarBolsa`, [`features/alocar_bolsa.feature`](../features/alocar_bolsa.feature) |
| Requisição (core) | [`requisicao/atender-e-entregar.md`](requisicao/atender-e-entregar.md) | `AtenderRequisicao`, `DespacharRequisicao`, `CancelarRequisicao`, `ConfirmarEntregaAoConcluirViagem` |
| Captação → Estoque | [`captacao/registrar-doacao.md`](captacao/registrar-doacao.md) | `RegistrarDoacao`, evento `DoacaoRegistrada`, `EntradaDeBolsaPorDoacao` |
| Estoque | [`estoque/reservar-bolsa.md`](estoque/reservar-bolsa.md) | agregado `Bolsa`, `ReservarBolsa` |
| Transporte | [`transporte/telemetria.md`](transporte/telemetria.md) | agregado `Viagem`, `TradutorSensorFabricante` (camada anticorrupção) |

Quem revisa: cada spec indica o responsável do squad pela revisão (mesma divisão da Parte 2).
