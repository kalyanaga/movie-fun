package org.superbiz.moviefun;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.superbiz.moviefun.albums.Album;
import org.superbiz.moviefun.albums.AlbumFixtures;
import org.superbiz.moviefun.albums.AlbumsBean;
import org.superbiz.moviefun.movies.Movie;
import org.superbiz.moviefun.movies.MovieFixtures;
import org.superbiz.moviefun.movies.MoviesBean;

import java.util.Map;

@Controller
public class HomeController {

    private final MoviesBean moviesBean;
    private final AlbumsBean albumsBean;
    private final MovieFixtures movieFixtures;
    private final AlbumFixtures albumFixtures;
    PlatformTransactionManager getMoviesPlatformTransactionManager;
    PlatformTransactionManager getAlbumsPlatformTransactionManager;

    private final TransactionTemplate moviesTemplate, albumsTemplate;

    public HomeController(MoviesBean moviesBean, AlbumsBean albumsBean, MovieFixtures movieFixtures, AlbumFixtures albumFixtures,
                          @Qualifier("getMoviesPlatformTransactionManager")
                                  PlatformTransactionManager getMoviesPlatformTransactionManager,
                          @Qualifier("getAlbumsPlatformTransactionManager")
                                  PlatformTransactionManager getAlbumsPlatformTransactionManager
    ) {
        this.moviesBean = moviesBean;
        this.albumsBean = albumsBean;
        this.movieFixtures = movieFixtures;
        this.albumFixtures = albumFixtures;
        this.getAlbumsPlatformTransactionManager = getAlbumsPlatformTransactionManager;
        this.getMoviesPlatformTransactionManager = getMoviesPlatformTransactionManager;

        this.moviesTemplate = new TransactionTemplate(getMoviesPlatformTransactionManager);
        this.albumsTemplate = new TransactionTemplate(getAlbumsPlatformTransactionManager);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/setup")
    public String setup(Map<String, Object> model) {

        for (Movie movie : movieFixtures.load()) {

            moviesTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    try {
                        moviesBean.addMovie(movie);
                    }catch(Exception e) {
                        e.printStackTrace();;
                    }
                }
            });
        }

        albumsTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                for (Album album : albumFixtures.load()) {
                    albumsBean.addAlbum(album);
                }
            }
        });

        model.put("movies", moviesBean.getMovies());
        model.put("albums", albumsBean.getAlbums());

        return "setup";
    }
}
