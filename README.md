# Loan Calculator API

API RESTFul para calcular prestações de empréstimos e simular cenários de financiamento

## Como executar

**Pré-requisitos:**
- Java 21+
- Maven 3.6+

**Execute a aplicação:**
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows (PowerShell)
./mvnw.cmd spring-boot:run

# Windows (Command Prompt)
mvnw.cmd spring-boot:run
```

## Endpoints

**Documentação completa disponível em:** http://localhost:8080/swagger-ui.html

### Loan Calculator

| Método | Endpoint              | Descrição                                     |
|--------|-----------------------|-----------------------------------------------|
| POST | `/api/loan-calculate` | Gerar relatório com parcelas do financiamento |

## Tecnologias

- **Java 21**
- **Spring Boot 3.5.4**
- **Spring Data JPA**
- **H2 Database**
- **Maven**
- **JUnit 5 + Mockito**
- **Lombok**
- **Bean Validation**
- **SpringDoc OpenAPI**

## Implementações Técnicas

### Arquitetura

A arquitetura segue o padrão **Layered Architecture** (Controller -> Service -> Repository)
```
src/main/java/com.github.xltgui/loancalculatorchallenge/
├── api/                    # 📄 Data Transfer Objects (DTOs)
│   ├── common/             # DTOs comuns para tratamento de exceções
│   │   ├── CustomFieldError.java
│   │   └── ResponseError.java
│   ├── LoanRequest.java   
│   └── PaymentDetailResponse.java
├── config/                 # ⚙️ Configurações da API
│   └── OpenApiConfiguration.java
├── exception/              # ⛔ Gerenciamento customizado de exceções
│   └── InvalidDateDateException.java
├── model/                  # 🎯 Regra de negócio principal e entidades
│   ├── Loan.java     
│   ├── LoanCalculationService.java 
│   ├── LoanCalculatorValidator.java  
│   └── PaymentDetail.java
├── repository/             # 💾 Interface de acesso a dados
│   ├── LoanRepository.java
│   └── PaymentDetailRepository.java
└── web/                    # 🌐 REST Controllers e entry points
    ├── common/             # Componentes comuns da camada web
    │   └── GlobalExceptionHandler.java
    ├── LoanCalculatorController.java
    ├── LoanCalculatorMapper.java
    └── Application.java
```

### Liberação da API para consumo externo (CORS)
* Esta API foi criada com o objetivo de ser consumida por um projeto front-end em Angular.
* Link: https://github.com/xltgui/loan-calculator-app.git
* O que é CORS?
  * CORS é um mecanismo de segurança implementado pelos navegadores web.
  * Por padrão, os navegadores impedem que scripts de uma página da web façam requisições para um domínio diferente daquele em que a página foi carregada. 
  * Essa restrição é conhecida como Política de Mesma Origem (Same-Origin Policy). 
* No contexto desta API, a aplicação front-end em Angular roda em um domínio diferente (`localhost:4200`) do que a API em Spring Boot (`localhost:8080`). Por isso, o navegador bloqueia as requisições por padrão.

* Como foi habilitado?
  * O mecanismo CORS foi habilitado nos controllers da API, utilizando a anotação `@CrossOrigin(origins = "http://localhost:4200")`
    * Isso faz com que a API adicione cabeçalhos HTTP específicos na resposta, como o  `Access-Control-Allow-Origin`, que autoriza o navegador a processar a requisição.

### Mapeamento de Objetos
* Mappers manuais   (`LoanCalculatorMapper`)
* Records utilizados para DTOs
  * Aproveita um dos benefícios do Java 21.
  * Redução de código boilerplate.
  * Criação de objetos imutáveis.


### Tratamento de Exceções
 * Exceções customizadas com `@ExceptionHandler`
 * Uso de records como DTOs para o trafego das informações referentes às exceções
   * Lista customizada de campos inválidos.

### Banco de Dados
* H2 Database
  * Dados são criados e salvos somente em tempo de execução.
* Obs.: Considero migrar para **PostgreSQL** com docker em implementações futuras.

### Documentação

* SpringDoc OpenAPI
  * Interface visual com **Swagger UI**.
  * Detalhes dos endpoints
    * Possíveis respostas para as requisições. 
  * Facilidade de consumo para os usuários da API.

### Testes
* Arquitetura
```
src/test/java/com.github.xltgui/loancalculatorchallenge/
  ├── integration/              # 🧪 Testes de integração
  │   └── LoanCalculatorIntegrationTest.java
  └── unit/                     # 🔬 Testes unitários
  ├── controller/       
  │   └── LoanCalculatorControllerUnitTest.java
  └── ApplicationTests.java
```

* Execute os testes:
```bash
# Linux/Mac
./mvnw test

# Windows (PowerShell)
./mvnw.cmd test

# Ou com Maven instalado (todas as plataformas)
mvn test
```



