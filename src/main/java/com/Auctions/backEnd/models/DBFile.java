package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "file")
public class DBFile implements Serializable {

    public static final long MAXIMUM_VIDEO_SIZE = 100000000L;
    public static final long MAXIMUM_IMAGE_SIZE = 10000000L;

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;


    @Column
    private String fileName;

    @Column
    private String fileType;

    @Column
    private String downloadLink;


    public DBFile(String fileName, String fileType, byte[] data) {
        this.fileName = fileName;
        this.fileType = fileType;
        // this.data = data;
    }
}
