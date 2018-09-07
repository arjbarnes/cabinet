package com.alexbarnes.cabinet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Entity
@Table(name="User")
public class User
{
  @Id
  @Column(name="Username")
  private String _username;

  @JsonIgnore
  @ElementCollection
  @CollectionTable(name="Intray", joinColumns=@JoinColumn(name="_username"))
  @Column(name="document_hash")
  private List<Document> _intray = new ArrayList<Document>();

  public String getUsername()
  {
    return _username;
  }

  public void setUsername(String username_)
  {
    _username = username_;
  }

  @JsonIgnore
  public List<Document> getIntray()
  {
    return _intray;
  }
}
