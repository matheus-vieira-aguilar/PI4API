package AcademiaGestaoWebApi.Manager;

import java.sql.Connection;
import java.util.UUID;

import AcademiaGestaoWebApi.Calculos.CalculosGerais;
import AcademiaGestaoWebApi.Calculos.PorcentagemDeGorduraCalculo;
import AcademiaGestaoWebApi.Config.ConnectionConfig;
import AcademiaGestaoWebApi.Models.Avaliacao;
import AcademiaGestaoWebApi.Models.RequestModels.AvaliacaoRequest;
import AcademiaGestaoWebApi.Models.ResponseModels.ApiRetorno;
import AcademiaGestaoWebApi.Repository.AvaliacaoDobrasRepository;
import AcademiaGestaoWebApi.Repository.AvaliacaoPerimetrosRepository;
import AcademiaGestaoWebApi.Repository.AvaliacaoPorcentagemGorduraRepository;
import AcademiaGestaoWebApi.Repository.AvaliacaoRepository;
import DTO.AvaliacaoDTO;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import static java.util.stream.Collectors.toList;

public class AvaliacaoManager {

    private final PorcentagemDeGorduraCalculo porcentagemDeGorduraCalculos;
    private final AvaliacaoRepository repositoryAvaliacao;
    private final AvaliacaoDobrasRepository repositoryDobras;
    private final AvaliacaoPerimetrosRepository repositoryPerimetros;
    private final AvaliacaoPorcentagemGorduraRepository repositoryGordura;
    private final Connection connection;

    public AvaliacaoManager() {
        connection = ConnectionConfig.getConnection(false);
        repositoryAvaliacao = new AvaliacaoRepository();
        repositoryDobras = new AvaliacaoDobrasRepository();
        repositoryPerimetros = new AvaliacaoPerimetrosRepository();
        repositoryGordura = new AvaliacaoPorcentagemGorduraRepository();
        porcentagemDeGorduraCalculos = new PorcentagemDeGorduraCalculo();
    }

    public ApiRetorno<Boolean> insertAvaliacao(AvaliacaoRequest avaliacaoRequest) throws Exception {
        ApiRetorno<Boolean> retorno = new ApiRetorno<>();

        try {
            Avaliacao avaliacao = RealizaCalculosAvalicao(avaliacaoRequest);
            avaliacao.setID(UUID.randomUUID());

            boolean sucesso = repositoryAvaliacao.insert(avaliacao, connection);

            if (!sucesso) {
                throw new Exception("Erro ao inserir a avaliação");
            }
            
            sucesso = repositoryDobras.insert(avaliacao.getDobrasAvaliacao(), connection);

            if (!sucesso) {
                throw new Exception("Erro ao inserir a avaliação");
            }
            
            sucesso = repositoryPerimetros.insert(avaliacao.getPerimetrosAvaliacao(), connection);
            
            if (!sucesso) {
                throw new Exception("Erro ao inserir a avaliação");
            }
            
            sucesso = repositoryGordura.insert(avaliacao.getPorcentagemDeGordura(), connection);

            if (!sucesso) {
                throw new Exception("Erro ao inserir a avaliação");
            }

            retorno.setData(sucesso);
            retorno.setSucess(sucesso);
            connection.commit();
            ConnectionConfig.closeConnection(connection);

            return retorno;
        } catch (Exception ex) {
            connection.rollback();
            ConnectionConfig.closeConnection(connection);
            throw ex;
        }
    }

    public ApiRetorno<Boolean> updateAvaliacao(AvaliacaoRequest avaliacaoRequest) throws Exception {
        ApiRetorno<Boolean> retorno = new ApiRetorno<>();

        try {
            Avaliacao avaliacao = RealizaCalculosAvalicao(avaliacaoRequest);
            avaliacao.setID(UUID.fromString(avaliacaoRequest.getId()));

            boolean sucesso = repositoryAvaliacao.update(avaliacao, connection);

            if (!sucesso) {
                throw new Exception("Erro ao atualizar a avaliação");
            }

            sucesso = repositoryDobras.update(avaliacao.getDobrasAvaliacao(), connection);

            if (!sucesso) {
                throw new Exception("Erro ao atualizar a avaliação");
            }

            sucesso = repositoryPerimetros.update(avaliacao.getPerimetrosAvaliacao(), connection);

            if (!sucesso) {
                throw new Exception("Erro ao atualizar a avaliação");
            }

            sucesso = repositoryGordura.update(avaliacao.getPorcentagemDeGordura(), connection);

            if (!sucesso) {
                throw new Exception("Erro ao atualizar a avaliação");
            }

            retorno.setData(sucesso);
            retorno.setSucess(sucesso);
            connection.commit();
            ConnectionConfig.closeConnection(connection);

            return retorno;
        } catch (Exception ex) {
            connection.rollback();
            ConnectionConfig.closeConnection(connection);
            throw ex;
        }
    }

    public ApiRetorno<Boolean> deleteAvaliacao(String id) throws SQLException, Exception {
        ApiRetorno<Boolean> retorno = new ApiRetorno<>();

        try {
            UUID idAvaliacao = UUID.fromString(id);

            boolean sucesso = repositoryDobras.delete(idAvaliacao, connection);

            if (!sucesso) {
                throw new Exception("Erro ao deletar avaliação");
            }

            sucesso = repositoryPerimetros.delete(idAvaliacao, connection);

            if (!sucesso) {
                throw new Exception("Erro ao deletar avaliação");
            }

            sucesso = repositoryGordura.delete(idAvaliacao, connection);

            if (!sucesso) {
                throw new Exception("Erro ao deletar avaliação");
            }

            sucesso = repositoryAvaliacao.delete(idAvaliacao, connection);

            if (!sucesso) {
                throw new Exception("Erro ao deletar avaliação");
            }

            retorno.setData(sucesso);
            retorno.setSucess(sucesso);
            connection.commit();
            ConnectionConfig.closeConnection(connection);

            return retorno;
        } catch (Exception ex) {
            connection.rollback();
            ConnectionConfig.closeConnection(connection);
            throw ex;
        }
    }

    public Avaliacao RealizaCalculosAvalicao(AvaliacaoRequest avaliacaoRequest) {
        Avaliacao avaliacao = Avaliacao.Factory.create(avaliacaoRequest);

        CalculosGerais calculosGerais = new CalculosGerais(avaliacaoRequest.getSexo());

        avaliacao.setImc(calculosGerais.imc(avaliacaoRequest.getMassa(), avaliacaoRequest.getEstatura()));
        avaliacao.setPccg(calculosGerais.pccq(avaliacaoRequest.getCintura(), avaliacaoRequest.getQuadril()));
        avaliacao.setMassaDeGordura(calculosGerais.massaDeGordura(avaliacaoRequest.getMassa()));
        avaliacao.setMassaMagra(calculosGerais.massaMagra(avaliacao.getMassa(), avaliacao.getMassaDeGordura()));
        avaliacao.setPesoIdeal(calculosGerais.pesoIdeal(avaliacao.getMassaMagra()));
        avaliacao.setPesoEmExcesso(calculosGerais.pesoExcesso(avaliacao.getMassa(), avaliacao.getPesoIdeal()));
        avaliacao.setPorcentagemDeGordura(porcentagemDeGorduraCalculos.executaCalculos(avaliacaoRequest));

        return avaliacao;
    }

    public List<AvaliacaoDTO> selectAvaliacao(UUID id) throws Exception {
        List<AvaliacaoDTO> avaliacoes = repositoryAvaliacao.select(id, connection);

        avaliacoes = avaliacoes
                .stream()
                .sorted(Comparator.comparing(AvaliacaoDTO::getDataAvaliacao).reversed())
                .collect(toList());

        int i = 1;
        
        for (AvaliacaoDTO avaliacao : avaliacoes) {
            avaliacao.setAvaliacao(i++);
        }

        for (AvaliacaoDTO avaliacao : avaliacoes) {           
            avaliacao.setPorcentagemDeGordura(repositoryGordura.select(avaliacao.getiD(), connection));
        }

        return avaliacoes;
    }
}
