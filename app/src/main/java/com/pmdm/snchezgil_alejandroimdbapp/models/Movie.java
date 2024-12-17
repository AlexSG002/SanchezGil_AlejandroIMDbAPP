package com.pmdm.snchezgil_alejandroimdbapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    private String imageUrl;
    private String title;
    private int rank;
    private String id;
    private String descripcion;

    public Movie() {}

    protected Movie(Parcel in) {
        imageUrl = in.readString();
        title = in.readString();
        rank = in.readInt();
        id = in.readString();
        descripcion = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getRank() {return rank;}
    public void setRank(int rank) {this.rank = rank;}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imageUrl);
        parcel.writeString(title);
        parcel.writeInt(rank);
        parcel.writeString(id);
        parcel.writeString(descripcion);
    }
}
