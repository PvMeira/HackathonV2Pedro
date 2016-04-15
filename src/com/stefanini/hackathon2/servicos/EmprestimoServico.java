package com.stefanini.hackathon2.servicos;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import javax.inject.Inject;

import com.stefanini.hackathon2.entidades.Emprestimo;
import com.stefanini.hackathon2.entidades.Livro;
import com.stefanini.hackathon2.repositorios.EmprestimoRepositorio;
import com.stefanini.hackathon2.transacao.Transacional;
import com.stefanini.hackathon2.util.Mensageiro;

public class EmprestimoServico {
	@Inject
	private EmprestimoRepositorio repositorio;

	@Transacional
	public void salvar(Emprestimo emprestimo) {
		if (emprestimo.getIdEmprestimo() == null) {
			if (emprestimo.getStatus() == null) {
				for (Livro confereEstoque : emprestimo.getLivros()) {
					if (confereEstoque.getEstoque() <= 1) {
						Mensageiro.notificaInformacao("Desculpe", "Sem livros em estoque!");
					} else {
						for (Livro livroDiminui : emprestimo.getLivros()) {
							livroDiminui.setEstoque(livroDiminui.getEstoque() - 1);
						}
						emprestimo.setDataEntrada(LocalDateTime.now());
						emprestimo.setStatus("ALUGADO");
						repositorio.inserir(emprestimo);
					}

				}

			}

		}
	}

	@Transacional
	public List<Emprestimo> carregarTodosEmprestimosDoBanco() {
		return repositorio.todosEmprestimos();
	}

	@Transacional
	public void deletar(Emprestimo emprestimo) {
		repositorio.remover(emprestimo);

	}

	@Transacional
	public void devolver(Emprestimo emprestimo) {
		if (emprestimo.getStatus() != null) {
			emprestimo.setStatus(null);
			emprestimo.setDataSaida(LocalDateTime.now());
			Duration dur = Duration.between(emprestimo.getDataEntrada(), emprestimo.getDataSaida());
			for (Livro livroAtribuirEstoque : emprestimo.getLivros()) {
				livroAtribuirEstoque.setEstoque(livroAtribuirEstoque.getEstoque() + 1);
			}
			if (dur.toDays() > 7) {
				emprestimo.setDiasAtrasados((int) dur.toDays() - 7);
				repositorio.devolver(emprestimo);
			} else {
				emprestimo.setDiasAtrasados(0);
				repositorio.devolver(emprestimo);
			}
		}
	}

}
