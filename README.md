# Projeto de Comparação de Técnicas de Planejamento de Caminho de Cobertura para VANTs

Este repositório contém o código e as implementações utilizadas no **projeto de comparação de técnicas de decomposição celular Boustrophedon e cobertura por árvore geradora** aplicadas ao planejamento de caminho de cobertura para **Veículos Aéreos Não Tripulados (VANTs)**. O objetivo principal deste projeto é avaliar a eficiência de diferentes abordagens de cobertura em missões de mapeamento, utilizando o **DJI Mobile SDK** e o **Firebase**.

## Acesso à Versão em Inglês

Para acessar a versão em inglês deste README, clique [aqui](/README.en.md).

## Demonstração do Aplicativo

Para assistir à demonstração do aplicativo, acesse o vídeo no [YouTube](https://youtu.be/x-V-9HLa1Ws).

## Objetivo do Projeto

O projeto visa realizar uma **análise comparativa** entre duas técnicas de planejamento de caminho de cobertura frequentemente utilizadas em mapeamento com VANTs:

- **Decomposição Celular Boustrophedon (BCD)**
- **Cobertura por Árvore Geradora (STC)**

A análise foca em métricas como a **distância percorrida**, **quantidade de fotos capturadas**, **número de curvas realizadas** e **tempo de execução**, tanto em ambientes simulados quanto em cenários reais, utilizando o drone **DJI Mavic Pro**.

## Funcionalidades Implementadas

- **Missões Autônomas**: Implementação de missões de voo com drones DJI, usando técnicas de cobertura BCD e STC.
- **Coleta de Dados**: Registro de dados como trajetórias de voo, imagens capturadas e performance da missão.
- **Simulação e Execução Real**: Testes de planejamento de voo em ambientes simulados e com drones reais.
- **Integração com Firebase**: Armazenamento de dados localmente e na nuvem para análise em tempo real.

## Tecnologias Utilizadas

- **DJI Mobile SDK 4.17**: Utilizado para controle de voo autônomo e captura de dados.
- **Firebase Realtime Database**: Armazenamento de dados de voo e imagens.
- **Android Studio**: Ambiente de desenvolvimento e ferramentas integradas para controle de versão (Git) e interface mobile.

## Resultados Preliminares

Durante os testes, foram observadas as seguintes características:

- O método BCD percorreu distâncias ligeiramente maiores, mas com melhor desempenho em termos de **tempo de execução**.
- O método STC capturou mais fotos, mas exigiu mais **curvas** e, consequentemente, maior tempo de execução em alguns cenários.
- Ambos os métodos apresentaram eficiências **semelhantes** em termos de cobertura total.

## Dependências

Adicione as seguintes dependências ao seu projeto Android para integrar o DJI SDK:

```groovy
implementation 'com.dji:dji-sdk:4.17'
provided 'com.dji:dji-sdk-provided:4.17'
```

## Como Executar o Projeto

1. **Clone o Repositório**

   - Abra o terminal (ou o prompt de comando) e execute o seguinte comando:
    ```
     git clone <URL_DO_REPOSITÓRIO>
    ```
   - Navegue até o diretório do projeto:
    ```
     cd nome_do_repositorio
    ```

2. **Configuração do arquivo `local.properties`**

   - O arquivo `local.properties` é gerado automaticamente pelo Android Studio. Não é necessário modificá-lo manualmente, mas você deve garantir que as seguintes chaves estejam presentes:
    ```
     sdk.dir=C\:\\Users\\Notebook\\AppData\\Local\\Android\\Sdk
     MAPS_API_KEY=SUACHAVE_API_AQUI
     DJI_API_KEY=SUACHAVE_API_AQUI
    ```
   - **Nota**: Substitua `SUACHAVE_API_AQUI` por suas respectivas chaves de API. As chaves são necessárias para acessar os serviços de mapas e o SDK da DJI.

3. **Instalação das Dependências**

   - Abra o projeto no Android Studio.
   - Aguarde enquanto o Android Studio sincroniza o projeto e baixa as dependências necessárias. Se houver alguma falha, verifique o arquivo `build.gradle` e faça as correções necessárias.

4. **Executar o Projeto**

   - Conecte um dispositivo Android ao computador ou inicie um emulador Android.
   - Selecione o dispositivo ou emulador na barra de ferramentas do Android Studio.
   - Clique no botão **Run** (ou pressione `Shift + F10`) para compilar e executar o aplicativo.

5. **Verificação e Teste**

   - Após a instalação, abra o aplicativo no dispositivo.
   - Certifique-se de que a funcionalidade de mapeamento e os recursos da DJI estão funcionando corretamente.

### Considerações Finais

- **Documentação**: Consulte a documentação oficial da DJI e do Google Maps para obter informações adicionais sobre as APIs utilizadas.
- **Erros Comuns**: Caso encontre erros durante a execução, verifique se as chaves de API estão corretas e se as permissões necessárias estão concedidas no dispositivo.

## Contribuição

Sinta-se à vontade para contribuir com melhorias ou sugerir novos recursos por meio de pull requests.

## Licença

Este projeto está licenciado sob a licença MIT - consulte o arquivo [LICENSE](LICENSE) para mais detalhes.
