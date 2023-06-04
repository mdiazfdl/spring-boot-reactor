package com.udemy.springbootreactor.app;

import com.udemy.springbootreactor.app.models.Comentarios;
import com.udemy.springbootreactor.app.models.Usuario;
import com.udemy.springbootreactor.app.models.UsuarioComentarios;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SpringBootReactorApplication.class, args);
    }

    @Override
    public void run(String... args) throws InterruptedException {
        Create();
        //InterumpirIntervaloInfinito();
        //IntervaloInfinito();
        //Delay();
        //Intervalos();
        //UsuarioComentarioZipWithRangos();
        //UsuarioComentarioZipWith2();
        //UsuarioComentarioZipWith1();
        //UsuarioComentarioFlatMap();
        //CollectList();
        //ToString();
        //FlatMap();
        //Iterable();
    }
    public void Create(){
        Flux.create(emitter ->{
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                private Integer contador = 0;
                @Override
                public void run() {
                    emitter.next(++contador);
                    if(contador >= 10){
                        timer.cancel();
                        emitter.complete();
                    }
                }
            }, 1000, 1000);
        })
                .doOnNext(e -> log.info(e.toString()))
                .doOnComplete(() -> log.info("Completado"))
                .subscribe();
    }
    public void InterumpirIntervaloInfinito() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        Flux.interval(Duration.ofSeconds(1))
                .doOnTerminate(latch::countDown)
                .flatMap(i -> {
                    if(i >= 5){
                        return Flux.error(new InterruptedException("Solo 5"));
                    }
                    return Flux.just(i);
                })
                //TODO: El operador ".retry(n)" permite realizar n reintentos luedo de encontrar un error
                .retry(3)
                .subscribe(i -> log.info(i.toString()),
                        e -> log.error(e.getMessage()));
        latch.await();

    }
    public void IntervaloInfinito(){
        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()))
                .blockLast();

    }
    public void Delay(){
        Flux<Integer> rango = Flux.range(1,12)
                .delayElements(Duration.ofSeconds(1))
                .doOnNext(i -> log.info(i.toString()));
        //TODO: El operador ".blockLast()" es un observador pero que bloquea hasta que todos los procesos terminen
        rango.blockLast();

    }
    public void Intervalos(){
        Flux<Integer> rango = Flux.range(1,12);
        Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));

        rango.zipWith(retraso,(ra,re) -> ra)
                .doOnNext(i -> log.info(i.toString()))
                .blockLast();
    }
    public void UsuarioComentarioZipWithRangos(){
        Flux.just(1, 2, 3, 4)
                .map(i -> (i*2))
                //TODO: El operador ".range()" crea un rango de numeros desde el start hasta el count.
                .zipWith(Flux.range(
                        0,4),(uno,dos) -> String.format("Primer Flux: %d, Segundo Flux %d", uno,dos))
                .subscribe(log::info);
    }
    public void UsuarioComentarioZipWith2(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Periquito", "Perez"));
        Mono<Comentarios> usuarioComentarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("comentario 1");
            comentarios.addComentario("comentario 2");
            comentarios.addComentario("comentario 3");
            return comentarios;
        });
        //TODO: El resultado del ".zipWith()" es un objeto tupla con los dos elementos,en T1 el resourse y en T2 el parametro
        usuarioMono.zipWith(usuarioComentarioMono)
                .map(tupla -> {
                    Usuario u = tupla.getT1();
                    Comentarios c = tupla.getT2();
                    return new UsuarioComentarios(u,c);
                })
                .subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));
    }
    public void UsuarioComentarioZipWith1(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Periquito", "Perez"));
        Mono<Comentarios> usuarioComentarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("comentario 1");
            comentarios.addComentario("comentario 2");
            comentarios.addComentario("comentario 3");
            return comentarios;
        });
        //TODO: El operador ".zipWith()" combina dos flujos
        usuarioMono.zipWith(usuarioComentarioMono,(u,c) -> (new UsuarioComentarios(u,c)))
                .subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));
    }
    public void UsuarioComentarioFlatMap(){
        Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("Periquito", "Perez"));
        Mono<Comentarios> usuarioComentarioMono = Mono.fromCallable(() -> {
            Comentarios comentarios = new Comentarios();
            comentarios.addComentario("comentario 1");
            comentarios.addComentario("comentario 2");
            comentarios.addComentario("comentario 3");
            return comentarios;
        });
        usuarioMono.flatMap(u -> usuarioComentarioMono.map(c -> (new UsuarioComentarios(u,c))))
                .subscribe(usuarioComentarios -> log.info(usuarioComentarios.toString()));
    }
    public void CollectList() {
        List<Usuario> users = new ArrayList<>();
        users.add(new Usuario("Pepe", "A"));
        users.add(new Usuario("Juan", "B"));
        users.add(new Usuario("Ana", "C"));
        users.add(new Usuario("Maria", "D"));
        users.add(new Usuario("Maria", "E"));
        Flux.fromIterable(users)
                //TODO: El operador ".collectList()" convierte un elemento Flux a Mono
                .collectList()
                .subscribe(lista -> log.info(lista.toString()));
    }
    public void ToString(){
        List<Usuario> users = new ArrayList<>();
        users.add(new Usuario("Pepe","A"));
        users.add(new Usuario("Juan", "B"));
        users.add(new Usuario("Ana","C"));
        users.add(new Usuario("Maria","D"));
        users.add(new Usuario("Maria","E"));
        Flux.fromIterable(users)
                //TODO: El operador ".map()", mapea a tipos de datos comunes (String, Objects...)
                .map(usuario -> usuario.getNombre().toUpperCase() + " " + (usuario.getApellido().toUpperCase()))
                //TODO: El operador ".flatMap()", mapea a tipos de datos observables (Flux o Mono)
                .flatMap(nombre -> {
                    if(nombre.contains("MARIA")) {
                        return Mono.just(nombre);
                    }else{
                        return Mono.empty(); //Convierte el objeto en un mono empty por lo cua lo elimina del flujo, actuando como un filter
                    }
                })
                .map(nombre -> nombre.toLowerCase())
                .subscribe(usuario -> log.info(usuario.toString()));
    }
    public void FlatMap(){
        List<String> users = new ArrayList<>();
        users.add("Pepe A");
        users.add("Juan B");
        users.add("Ana C");
        users.add("Maria D");
        users.add("Maria E");
        Flux.fromIterable(users)
                //TODO: El operador ".map()", mapea a tipos de datos comunes (String, Objects...)
                .map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
                //TODO: El operador ".flatMap()", mapea a tipos de datos observables (Flux o Mono)
                .flatMap(usuario -> {
                    if(usuario.getNombre().equalsIgnoreCase("maria")) {
                        return Mono.just(usuario);
                    }else{
                        return Mono.empty(); //Convierte el objeto en un mono empty por lo cua lo elimina del flujo, actuando como un filter
                    }
                })
                .map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                })
                .subscribe(usuario -> log.info(usuario.toString()));
    }
    public void Iterable(){
        List<String> users = new ArrayList<>();
        users.add("Pepe A");
        users.add("Juan B");
        users.add("Ana C");
        users.add("Maria D");
        users.add("Maria E");
        Flux<String> nombres = Flux.fromIterable(users);
        Flux<Usuario> usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(),nombre.split(" ")[1].toUpperCase()))
                //TODO: El operador ".filter()" se evalua una funcion buleana, si se cumple, esos son los elemnetos que continuan en el flujo
                .filter(usuario -> usuario.getNombre().equalsIgnoreCase("maria"))
                .doOnNext(usuario -> {
                    if (usuario == null) {
                        throw new RuntimeException("los nombres no pueden estar vacios");
                    }
                    System.out.println(usuario.getNombre());
                })
                .map(usuario -> {
                    String nombre = usuario.getNombre().toLowerCase();
                    usuario.setNombre(nombre);
                    return usuario;
                });

        usuarios.subscribe(usuario -> log.info(usuario.toString()),  //lanza un log por cada elemento
                error -> log.error(error.getMessage()),
                () -> log.info("Ha terminado la ejecusion del flujo"));
    }
}
