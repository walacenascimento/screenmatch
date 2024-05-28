package br.com.alura.screenmatch.services;

public interface IConverteDados {
    // <T> T declaração de generics
    <T> T obterDados(String json, Class<T> classe);
}
