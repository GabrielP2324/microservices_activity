package management.users.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class UserSecurity {

        @Autowired
        JwtConverter jwtConverter;

        // Define filtros relacionados a seguranca
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http.csrf(csrf -> csrf.disable());
                http.authorizeHttpRequests(authz -> authz
                                .requestMatchers(HttpMethod.GET, "/user/token").permitAll()
                                .requestMatchers(HttpMethod.POST, "/user").permitAll()
                                .requestMatchers(HttpMethod.GET, "/user").permitAll()
                                .requestMatchers(HttpMethod.PUT, "/user").permitAll()
                                .requestMatchers(HttpMethod.GET, "/user/bloqueados").permitAll()
                                .requestMatchers("/user/unblock/**").hasAnyRole("ADMIN"));
                // autenticação agora somente com Bearer Token
                // .httpBasic(Customizer.withDefaults());

                http.sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));
                return http.build();

        }

        // Cria os usuários em memória
        // @Bean
        public InMemoryUserDetailsManager userDetailsService(PasswordEncoder passwordEncoder) {

                System.out.println("------> " + passwordEncoder.encode("password"));

                // cria um usuário normal
                UserDetails user = User.withUsername("user")
                                .password(passwordEncoder.encode("password"))
                                .roles("USER")
                                .build();

                // cria um usuário admin - role admin
                UserDetails admin = User.withUsername("admin")
                                .password(passwordEncoder.encode("admin"))
                                .roles("USER", "ADMIN")
                                .build();

                return new InMemoryUserDetailsManager(user, admin);
        }

        // define como a senha será criptografada
        @Bean
        public PasswordEncoder passwordEncoder() {
                PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
                return encoder;
        }
        /*
         * @Bean
         * public UserDetailsManager users(DataSource dataSource, PasswordEncoder
         * passwordEncoder) {
         * 
         * UserDetails teste = User.builder()
         * .username("teste")
         * .password(passwordEncoder.encode("teste"))
         * .roles("USER")
         * .build();
         * 
         * UserDetails user = User.builder()
         * .username("user")
         * .password(passwordEncoder.encode("password"))
         * .roles("USER")
         * .build();
         * 
         * UserDetails admin = User.builder()
         * .username("admin")
         * .password(passwordEncoder.encode("password"))
         * .roles("USER", "ADMIN")
         * .build();
         * JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
         * users.createUser(user);
         * users.createUser(admin);
         * users.createUser(teste);
         * 
         * return users;
         * }
         */
}
