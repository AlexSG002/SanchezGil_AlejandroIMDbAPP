package com.pmdm.snchezgil_alejandroimdbapp.models;

import java.util.List;

public class PopularMoviesResponse {
    private List<Movie> items;

    public List<Movie> getItems() {
        return items;
    }
    public void setItems(List<Movie> items) {
        this.items = items;
    }
}
