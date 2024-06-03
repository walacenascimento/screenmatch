package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.services.ConsumoApi;
import br.com.alura.screenmatch.services.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=e465ef95";

    public void exibeMenu() {
        System.out.println("Digite o nome da serie para busca ");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        // Criando uma lista para adicionar as temporadas
        List<DadosTemporada> temporadas = new ArrayList<>();

        // Percorrendo todas as temporadas com laço FOR
        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        // percorrendo toda a lista e imprimindo o resultado
        temporadas.forEach(System.out::println);

        // Percorrendo a lista de temporadas e episodios e imprimindo os episódios
//        for(int i= 0; i < dados.totalTemporadas(); i++){
//            List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//            for (int j = 0; j < episodiosTemporada.size(); j++){
//                System.out.println(episodiosTemporada.get(j).titulo());
//            }
//        }

        /* Percorrendo a lista de temporadas e episodios e imprimindo os episódios de forma
        mais simples com lambdas
         */
        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

//        // Trabalhando com o stream - exemplo
//        List<String> nomes = Arrays.asList("jaque", "Iasmin", "Paulo", "Nico");
//        // chamando o stream, ordenando a lista, limitando, percorrendo e imprimindo.
//        nomes.stream()
//                .sorted()
//                .limit(3)
//                .filter(n->n.startsWith("N"))
//                .map(n-> n.toUpperCase())
//                .forEach(System.out::println);
//        // O reultado será a impressão do nome "NICO"

        // buscando o TOP 5 Episódios buscando pela avaliação
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t-> t.episodios().stream())
                .collect(Collectors.toList());

//
//        System.out.println("\n TOP 5 EPISÓDIOS");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println("Primeiro filtro(N/A) " + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(e -> System.out.println("Ordenação " + e))
//                .limit(10)
//                .peek(e -> System.out.println("Limite " + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Mapeamento " + e))
//                .forEach(System.out::println);


        // Filtrando e ordenando episódios
        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d)))
                .collect(Collectors.toList());

        episodios.forEach(System.out::println);

        // Encontrando a primeira ocorrencia de uma busca em uma coleção
        System.out.println("Digite um trecho do título do episódio para a buscá-lo: ");
        var trechoTitulo = leitura.nextLine();
        Optional<Episodio> episodiobuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if (episodiobuscado.isPresent()){
            System.out.println("Episódio encontrado!");
            System.out.println("Temporada: " + episodiobuscado.get().getTemporada());
        } else {
            System.out.println("Episódio não encotrando!");
        }



////        // filtrando os episódios por data
//        System.out.println("A partir de que ano você deseja ver os episódios? ");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                .forEach(e -> System.out.println(
//                        "Temporada: " + e.getTemporada() +
//                                "Episódio: " + e.getTitulo() +
//                                "Data lançamento: " + e.getDataLancamento().format(formatador)
//                ));

        // Criando um mapa com dados por temporada
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));
        System.out.println(avaliacoesPorTemporada);

        // Coletando estatísticas com a classe DoubleSummaryStatistics
        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
        System.out.println("média: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Quantidade: " + est.getCount());

    }
}