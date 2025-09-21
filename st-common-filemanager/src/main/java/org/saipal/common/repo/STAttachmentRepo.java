package org.saipal.common.repo;


import org.saipal.common.entity.STAttachment;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface STAttachmentRepo extends MongoRepository<STAttachment, Long> {

}
