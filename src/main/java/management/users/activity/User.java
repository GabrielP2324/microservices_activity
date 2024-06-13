package management.users.activity;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/user")
public class User {

  @Autowired
  private UserDAO dao;

  @Autowired
  private FailedLoginKafkaProducer kafkaProducer;
  // Existing code

  @PostMapping()
  public ResponseEntity<String> createUser(@RequestBody UserBean user) throws DupedIdException {

    if (dao.count() == 0) {
      System.out.println("Não há usuários");
      dao.save(user);
      return new ResponseEntity<String>(user.getUsername(), HttpStatus.CREATED);
    } else if (dao.findByUsername(user.getUsername()) != null) {
      System.out.println("Usuário já existe");
      return new ResponseEntity<String>("Usuário ja existente", HttpStatus.NOT_ACCEPTABLE);
    }
    System.out.println("Usuário não existe");
    user.setTotalFails(0);
    user.setTotalLogins(0);
    System.out.println(user);
    dao.save(user);
    return new ResponseEntity<String>(user.getUsername(), HttpStatus.CREATED);

  }

  @GetMapping()
  public ResponseEntity<Iterable<UserBean>> todosUsuarios() {
    System.out.println(dao.count());

    if (dao.count() > 0) {
      return new ResponseEntity<Iterable<UserBean>>(dao.findAll(), HttpStatus.OK);
    } else {
      System.out.println("Não há usuários");
      return new ResponseEntity<Iterable<UserBean>>(HttpStatus.NO_CONTENT);
    }
  }

  @PutMapping()
  public ResponseEntity<String> updateUser(@RequestBody UserBean user) {
    if (user.getUsername() == null || user.getPassword() == null) {
      return new ResponseEntity<String>("Usuário e senha não podem ser nulos", HttpStatus.BAD_REQUEST);
    }

    UserBean userExists = dao.findByUsername(user.getUsername());
    if (userExists == null) {
      System.out.println("Usuário não existe");
      return new ResponseEntity<String>("Usuário não existe", HttpStatus.UNAUTHORIZED);
    }

    if (userExists.isBlocked()) {
      System.out.println("Usuário bloqueado");
      return new ResponseEntity<String>("Usuário bloqueado", HttpStatus.UNAUTHORIZED);
    }
    try {
      if (userExists.getTotalLogins() > 10) {
        System.out.println("10 logins já foram feitos, favor alterar senha");
        return new ResponseEntity<String>("Usuário deve alterar a senha para logar novamente", HttpStatus.UNAUTHORIZED);
      }
    } catch (Exception e) {
    }

    if (!userExists.getPassword().equals(user.getPassword())) {
      System.out.println("Senha incorreta");

      try {
        userExists.setTotalFails(userExists.getTotalFails() + 1);
      } catch (Exception e) {
        userExists.setTotalFails(1);
      }

      if (userExists.getTotalFails() > 5) {
        userExists.setBlocked(true);
        System.out.println("Usuário bloqueado");
      }

      dao.save(userExists);
      kafkaProducer.sendMessage(userExists.getUsername());
      return new ResponseEntity<String>("Senha incorreta",
          HttpStatus.UNAUTHORIZED);
    }

    try {
      userExists.setTotalLogins(userExists.getTotalLogins() + 1);
    } catch (Exception e) {
      userExists.setTotalLogins(1);
    }

    dao.save(userExists);
    return new ResponseEntity<String>("Login efetuado", HttpStatus.OK);
  }

  @GetMapping("/bloqueados")
  public ResponseEntity<UserBean[]> usuariosBloqueados() {
    UserBean[] bloqueados = dao.findByBlockedTrue();

    System.out.println(bloqueados.length);

    if (bloqueados.length > 0) {
      return new ResponseEntity<UserBean[]>(bloqueados, HttpStatus.OK);
    } else {
      System.out.println("Não há usuários bloqueados");
      return new ResponseEntity<UserBean[]>(HttpStatus.NO_CONTENT);
    }
  }

  @PutMapping("/trocasenha")
  public ResponseEntity<String> trocaSenha(@RequestBody PasswordChangeBean user) {
    UserBean userExists = dao.findByUsername(user.getUsername());

    if (userExists == null) {
      System.out.println("Usuário não existe");
      return new ResponseEntity<String>("Usuário não existe", HttpStatus.NOT_FOUND);
    }

    if (userExists.isBlocked()) {
      System.out.println("Usuário bloqueado");
      return new ResponseEntity<String>("Usuário bloqueado", HttpStatus.UNAUTHORIZED);
    }

    if (!userExists.getPassword().equals(user.getCurrentPassword())) {
      System.out.println("Senha atual incorreta");
      return new ResponseEntity<String>("Senha atual incorreta", HttpStatus.UNAUTHORIZED);
    }

    if (userExists.getPassword().equals(user.getNewPassword())) {
      System.out.println("Senha atual e nova são iguais");
      return new ResponseEntity<String>("Senha atual e nova são iguais", HttpStatus.BAD_REQUEST);
    }

    userExists.setPassword(user.getNewPassword());
    userExists.setTotalLogins(0);
    dao.save(userExists);
    return new ResponseEntity<String>("Senha alterada", HttpStatus.OK);
  }

  @PostMapping("/unblock/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> unblock(@PathVariable String username) {
      if (username == null) {
          return new ResponseEntity<String>("Usuário não pode ser nulo", HttpStatus.BAD_REQUEST);
      }
      UserBean user = dao.findByUsername(username);
      System.out.println(user);
      if (user == null) {
          return new ResponseEntity<String>("Usuário não existe", HttpStatus.NOT_FOUND);
      }
      if (!user.isBlocked()) {
          return new ResponseEntity<String>("Usuário não está bloqueado", HttpStatus.BAD_REQUEST);
      }
      user.setTotalFails(0);
      user.setBlocked(false);
      dao.save(user);
      return new ResponseEntity<String>("Usuário desbloqueado", HttpStatus.OK);
  }
  
}
