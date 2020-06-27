package batch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "object_key_info")
public class ObjectKeyInfo implements Serializable {

    @Id
    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;
}
