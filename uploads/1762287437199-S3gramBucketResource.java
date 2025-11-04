package br.com.valenstech.s3gram.shared.webdav;

import br.com.valenstech.s3gram.app.object.ObjectService;
import br.com.valenstech.s3gram.app.object.dto.ObjectSummary;
import io.milton.http.Auth;
import io.milton.http.Request;
import io.milton.resource.CollectionResource;
import io.milton.resource.Resource;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class S3gramBucketResource implements CollectionResource {

    private final String bucketName;
    private final ObjectService objectService;

    public S3gramBucketResource(String bucketName, ObjectService objectService) {
        this.bucketName = bucketName;
        this.objectService = objectService;
    }

    @Override
    public String checkRedirect(Request request) {
        return null;
    }

    @Override
    public List<? extends Resource> getChildren() {
        List<ObjectSummary> summaries = objectService.listObjects(bucketName);

        return summaries.stream()
                .map(s -> new S3gramFileResource(bucketName, s.objectKey(), s, objectService))
                .collect(Collectors.toList());
    }

    @Override
    public Resource child(String childName) {
        // Se quiser suportar subpastas de verdade depois, aqui será tratado
        return null;
    }

    @Override
    public String getName() {
        return bucketName;
    }

    @Override
    public String getUniqueId() {
        return "bucket_" + bucketName;
    }

    @Override
    public Date getModifiedDate() {
        return new Date();
    }

    @Override
    public Date getCreateDate() {
        return null;
    }

    @Override
    public Object authenticate(String user, String password) {
        return "ok";
    }

    @Override
    public boolean authorise(Request request, Request.Method method, Auth auth) {
        return true;
    }

    @Override
    public String getRealm() {
        return "S3gramWebDAV";
    }
}
