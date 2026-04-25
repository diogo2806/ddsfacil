package br.com.ddsfacil.conteudo.infrastructure;

import br.com.ddsfacil.conteudo.domain.ConteudoDdsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConteudoDdsRepository extends JpaRepository<ConteudoDdsEntity, Long> {
}
