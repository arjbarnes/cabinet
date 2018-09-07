package com.alexbarnes.cabinet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="Tag")
public class Tag
{
  @Id
  @Column(name="Name")
  private String _name;

  @ManyToMany
  @JoinTable(name="TaggedDocuments",
             joinColumns={@JoinColumn(name="TagName")},
             inverseJoinColumns={@JoinColumn(name="DocumentHash")})
  private List<Document> _documents = new ArrayList<Document>();

  public Tag()
  {

  }

  public String getName()
  {
    return _name;
  }

  public void setName(String name_)
  {
    _name = name_;
  }

  public Collection<Document> getDocuments()
  {
    return _documents;
  }
}
