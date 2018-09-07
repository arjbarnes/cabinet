package com.alexbarnes.cabinet;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="Document")
public class Document
{
  public static String getExtensionForMimeType(String mimeType_)
  {
    // ToDo: Expand number of supported MimeTypes
    switch(mimeType_)
    {
      case "application/pdf":
        return ".pdf";
      default:
        return "";
    }
  }

  @Id
  @Column(name="Hash")
  private String _hash;

  @Column(name="Name")
  private String _name;

  @Column(name="Date")
  private Date _date;

  @Column(name="InIntray")
  private boolean _inIntray=true;

  @Column(name="IsArchived")
  private boolean _isArchived=false;

  @ManyToMany(mappedBy="_documents")
  private List<Tag> _tags = new ArrayList<Tag>();

  public Document()
  {

  }

  public String getHash()
  {
    return _hash;
  }

  public void setHash(String hash_)
  {
    _hash = hash_;
  }

  public String getName()
  {
    return _name;
  }

  public void setName(String name_)
  {
    _name = name_;
  }

  public Date getDate()
  {
    return _date;
  }

  public void setDate(Date date_)
  {
    _date = date_;
  }

  public boolean getInIntray()
  {
    return _inIntray;
  }

  public void setInIntray(boolean inIntray_)
  {
    _inIntray = inIntray_;
  }

  public boolean getIsArchived()
  {
    return _isArchived;
  }

  public void setIsArchived(boolean isArchived_)
  {
    _isArchived = isArchived_;
  }

  public List<Tag> getTags()
  {
    return _tags;
  }

  public void setTags(List<Tag> tags_)
  {
    _tags = tags_;
  }
}
