package com.pmdm.snchezgil_alejandroimdbapp.models;

public class MovieOverviewResponse {
    private String title;
    private String plot;
    private String image;
    private int rank;

    public String getTitle() {return title;}
    public String getPlot() {return plot;}
    public String getImage() {return image;}
    public int getRank() {return rank;}

    public void setTitle(String title) {this.title = title;}
    public void setPlot(String plot) {this.plot = plot;}
    public void setImage(String image) {this.image = image;}
    public void setRank(int rank) {this.rank = rank;}
}
