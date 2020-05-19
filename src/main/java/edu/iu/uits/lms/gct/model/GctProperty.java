package edu.iu.uits.lms.gct.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by chmaurer on 5/15/20.
 */
@Entity
@Table(name = "GCT_PROPERTIES")
@SequenceGenerator(name = "GCT_PROPERTIES_ID_SEQ", sequenceName = "GCT_PROPERTIES_ID_SEQ", allocationSize = 1)
@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class GctProperty implements Serializable {

    @Id
    @Column(name = "GCT_PROPERTIES_ID")
    @GeneratedValue(generator = "GCT_PROPERTIES_ID_SEQ")
    private Long id;

    @NonNull
    @Column(name = "prop_key")
    private String key;

    @NonNull
    @Column(name = "prop_value")
    private String value;

}
