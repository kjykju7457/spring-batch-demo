package batch.domain;

import lombok.Builder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "object_key_info")
public class ObjectKeyInfo implements Serializable {

    @Id
    @Column
    private String objectKey;

    @Column
    private LocalDateTime lastModified;
}
