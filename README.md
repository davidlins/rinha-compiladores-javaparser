# rinha-compiladores-javaparser

Após [@zanfranceschi](https://twitter.com/zanfranceschi) lançar a [rinha de backend](https://github.com/zanfranceschi/rinha-de-backend-2023-q3), essas duas meninas [Gabi](https://twitter.com/algebraic_gabi) e [Sofia](https://twitter.com/algebraic_sofia) que são totalmente fora da curva, lançarem a [rinha de compiladores](https://github.com/aripiprazole/rinha-de-compiler)

A brincadeira consiste ou compilar a linguagem Rinha criada por elas, para isso eu implementei um [Source-to-source compiler](https://en.wikipedia.org/wiki/Source-to-source_compiler), mas acho que estária mais para Ast-to-source compiler, pois não gero a AST - [Abstract syntax tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree), ela é gerada por uma ferramenta delas implementadas em Rust.

 

## Fontes

Partindo do principio basico, nada se cria tudo se copia, além das fontes que estão no próprio repo da [rinha de compiladores](https://github.com/aripiprazole/rinha-de-compiler), segue algumas outras que me ajudaram: 

* O [Rodrigo Navarro](https://twitter.com/rdrnavarro) fez uma live implementando a solução dele em Rust <https://www.youtube.com/watch?v=FbCdhicY3sk&t=5473s>;
* Meu pupilo [Leandro](https://twitter.com/leandronsp) também fez uma live com a implementação em Ruby  <https://www.youtube.com/watch?v=fIFslRgxKXk>;
* Para geração dos códigos fontes em java, usei a biblioteca [JavaParser](https://javaparser.org);
* Um dos dev do JavaParser é o [Federico Tomassetti](https://twitter.com/ftomasse) da [Strumenta](https://strumenta.com/) o cara manja desses paranauê de compiladores, aqui tem vários [artigos](https://tomassetti.me/?_ga=2.191813482.1372844612.1694702004-395410319.1694702004);
* Como eu fiz para compilar em memória os códigos gerados e rodar https://www.baeldung.com/java-string-compile-execute-code;
* A princípio pensei em usar o [Truffle](https://www.graalvm.org/latest/graalvm-as-a-platform/language-implementation-framework/) usando como referencia os [SimpleLanguage](https://www.graalvm.org/latest/graalvm-as-a-platform/implement-language), aqui um [tutorial](https://www.graalvm.org/latest/graalvm-as-a-platform/language-implementation-framework/LanguageTutorial/);

