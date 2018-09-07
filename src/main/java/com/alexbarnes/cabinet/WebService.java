package com.alexbarnes.cabinet;

import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/")
public class WebService
{
  @GET
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<User> listUsers()
  {
    EntityManager em = App.getEntityManager();
    List<User> users = em.createQuery("FROM User").getResultList();
    return users != null ? users : new ArrayList<User>();
  }

  @POST
  @Path("/users")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public User createTag(User user_)
  {
    EntityManager em = App.getEntityManager();
    em.getTransaction().begin();
    em.persist(user_);
    em.getTransaction().commit();
    return user_;
  }

  @GET
  @Path("/documents")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Document> listDocuments()
  {
    EntityManager em = App.getEntityManager();
    List<Document> documents = em.createQuery("FROM Document").getResultList();
    return documents != null ? documents : new ArrayList<Document>();
  }

  @POST
  @Path("/documents")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Document createDocument(@FormDataParam("data") FormDataBodyPart data_, @FormDataParam("meta") FormDataBodyPart meta_)
  {
    try
    {
      InputStream is = data_.getValueAs(InputStream.class);
      byte[] data = ByteStreams.toByteArray(is);
      String hash = Hashing.sha256().hashBytes(data).toString();

      EntityManager em = App.getEntityManager();
      Document doc = em.find(Document.class, hash);
      File file = new File(App.getFileStorageLocation() + "/" + hash);

      if(file.exists() && doc != null)
      {
        throw new WebApplicationException(Response.Status.CONFLICT);
      }
      else
      {
        doc = new Document();
        doc.setHash(hash);
        doc.setInIntray(true);
        doc.setIsArchived(false);
        if(meta_ != null)
        {
          meta_.setMediaType(MediaType.APPLICATION_JSON_TYPE);
          Document meta = meta_.getValueAs(Document.class);
          doc.setName(meta.getName());
        }
        try
        {
          em.getTransaction().begin();
          em.persist(doc);
          OutputStream os = new FileOutputStream(file);
          os.write(data);
          os.close();
          em.getTransaction().commit();
          return doc;
        }
        catch(IOException e_)
        {
          em.getTransaction().rollback();
          throw new WebApplicationException();
        }
      }
    }
    catch(IOException e_)
    {
      //ToDo Improve
      throw new WebApplicationException();
    }
  }

  @GET
  @Path("/documents/{hash}")
  public Response retrieveDocument(@PathParam("hash") String hash_)
  {
    EntityManager em = App.getEntityManager();
    Document document = em.find(Document.class, hash_);
    if(document == null)
    {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    else
    {
      try
      {
        File file = new File(App.getFileStorageLocation() + "/" + document.getHash());
        String filename = document.getName() != null ? document.getName() : document.getHash();
        String mimeType = Files.probeContentType(file.toPath());
        String extension = Document.getExtensionForMimeType(mimeType);
        return Response.ok(file, mimeType).header("Content-Disposition", "filename=\"" + filename + extension + "\"").build();
      }
      catch(IOException e_)
      {
        //ToDo Improve
        throw new WebApplicationException();
      }
    }
  }

  @PUT
  @Path("/documents/{hash}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response retrieveDocument(@PathParam("hash") String hash_, Document document_)
  {
    EntityManager em = App.getEntityManager();
    Document document = em.find(Document.class, hash_);
    if(document == null)
    {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    else
    {
      document.setName(document_.getName());
      document.setDate(document_.getDate());
      document.setTags(document_.getTags());
      try
      {
        em.getTransaction().begin();
        em.persist(document);
        em.getTransaction().commit();
        return Response.ok().build();
      }
      catch(Exception e_)
      {
        em.getTransaction().rollback();
        throw new WebApplicationException();
      }
    }
  }

  @DELETE
  @Path("/documents/{hash}")
  public Response deleteDocument(@PathParam("hash") String hash_)
  {
    EntityManager em = App.getEntityManager();
    Document document = em.find(Document.class, hash_);
    if(document == null)
    {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    else
    {
      try
      {
        em.getTransaction().begin();
        em.remove(document);
        File file = new File(App.getFileStorageLocation() + "/" + document.getHash());
        if(file.exists())
        {
          file.delete();
        }
        em.getTransaction().commit();
        return Response.ok().build();
      }
      catch(Exception e_)
      {
        em.getTransaction().rollback();
        throw new WebApplicationException();
      }
    }
  }

  @GET
  @Path("/intray")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Document> listDocumentsInIntray()
  {
    EntityManager em = App.getEntityManager();
    List<Document> documents = em.createQuery("FROM Document WHERE _inIntray=true").getResultList();
    return documents != null ? documents : new ArrayList<Document>();
  }

  @GET
  @Path("/tags")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Tag> listTags()
  {
    EntityManager em = App.getEntityManager();
    List<Tag> tags = em.createQuery("FROM Tag").getResultList();
    return tags != null ? tags : new ArrayList<Tag>();
  }

  @POST
  @Path("/tags")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Tag createTag(Tag tag_)
  {
    EntityManager em = App.getEntityManager();
    em.getTransaction().begin();
    em.persist(tag_);
    em.getTransaction().commit();
    return tag_;
  }
}
