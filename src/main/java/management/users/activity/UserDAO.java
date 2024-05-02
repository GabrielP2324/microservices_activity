package management.users.activity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDAO extends CrudRepository<UserBean, String> {

    // sempre inicia com findBy...
    // incluir nome do atributo
    UserBean findByUsername(String username);

    Iterable<UserBean> findByUsernameAndPassword(String username, String senha);

    UserBean[] findByBlockedTrue();

    // // SELECT * FROM TAB_CURSO WHERE CURSO = ? AND TURMA = ?
    // Iterable<UsuarioBean> findByCursoAndTurma(String curso, String turma);

    // // SELECT * FROM TAB_ALUNO WHERE NOME LIKE ?
    // Iterable<UsuarioBean> findByNomeLike(String nome);

    // @Query("select a.id from TAB_ALUNO a")
    // Iterable<Integer> minhaConsulta();

}
