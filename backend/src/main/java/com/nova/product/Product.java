package com.nova.product;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity
@Table(name="products", indexes={@Index(name="idx_product_category",columnList="category"),@Index(name="idx_product_name",columnList="name")})
public class Product {
 @Id private Long id;
 @Column(nullable=false) private String name;
 @Column(nullable=false,unique=true) private String slug;
 @Column(nullable=false) private String category;
 @Column(nullable=false) private String brand;
 @Column(nullable=false,precision=12,scale=2) private BigDecimal price;
 private Double rating;
 private Integer reviewCount;
 @Column(length=1200) private String description;
 @Column(length=800) private String imageUrl;
 private String color;
 private Boolean featured=false;
 @Column(length=800) private String tags;
 @Column(name="available_sizes", length=200) private String availableSizes;
 protected Product() {}
 public Product(Long id,String name,String slug,String category,String brand,BigDecimal price,Double rating,Integer reviewCount,String description,String imageUrl,String color,Boolean featured,String tags){
  this(id,name,slug,category,brand,price,rating,reviewCount,description,imageUrl,color,featured,tags,null);
 }
 public Product(Long id,String name,String slug,String category,String brand,BigDecimal price,Double rating,Integer reviewCount,String description,String imageUrl,String color,Boolean featured,String tags,String availableSizes){
  this.id=id;this.name=name;this.slug=slug;this.category=category;this.brand=brand;this.price=price;this.rating=rating;this.reviewCount=reviewCount;this.description=description;this.imageUrl=imageUrl;this.color=color;this.featured=featured;this.tags=tags;this.availableSizes=availableSizes;
 }
 public Long getId(){return id;} public String getName(){return name;} public String getSlug(){return slug;} public String getCategory(){return category;} public String getBrand(){return brand;} public BigDecimal getPrice(){return price;} public Double getRating(){return rating;} public Integer getReviewCount(){return reviewCount;} public String getDescription(){return description;} public String getImageUrl(){return imageUrl;} public String getColor(){return color;} public Boolean getFeatured(){return featured;} public String getTags(){return tags;}
 public String getAvailableSizes(){return availableSizes;}
 public void setAvailableSizes(String availableSizes){this.availableSizes=availableSizes;}
}
