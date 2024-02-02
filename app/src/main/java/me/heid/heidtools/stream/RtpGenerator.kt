package me.heid.heidtools.stream

class RtpGenerator {
    /*
    P – The length of this field is 1-bit. If value is 1, then it denotes presence of padding
    at end of packet and if value is 0, then there is no padding.

    X – The length of this field is also 1-bit. If value of this field is set to 1, then its
    indicates an extra extension header between data and basic header and if value is 0 then,
    there is no extra extension.
    Contributor count – This 4-bit field indicates number of contributors. Here maximum possible
    number of contributor is 15 as a 4-bit field can allows number from 0 to 15.

    M – The length of this field is 1-bit and it is used as end marker by application to indicate
    end of its data.
    Payload types – This field is of length 7-bit to indicate type of payload. We list applications
    of some common types of payload.

    Sequence Number – The length of this field is 16 bits. It is used to give serial numbers to RTP
    packets. It helps in sequencing. The sequence number for first packet is given a random number
    and then every next packet’s sequence number is incremented by 1. This field mainly helps in
    checking lost packets and order mismatch.

    Time Stamp – The length of this field is 32-bit. It is used to find relationship between times
    of different RTP packets. The timestamp for first packet is given randomly and then time stamp
    for next packets given by sum of previous timestamp and time taken to produce first byte of
    current packet. The value of 1 clock tick is varying from application to application.
    Synchronization Source Identifier – This is a 32-bit field used to identify and define the
    source. The value for this source identifier is a random number that is chosen by source itself.
     This mainly helps in solving conflict arises when two sources started with the same
     sequencing number.

    Contributor Identifier – This is also a 32-bit field used for source identification where there
    is more than one source present in session. The mixer source use Synchronization source
    identifier and other remaining sources (maximum 15) use Contributor identifier.
     0                   1                   2                   3
      0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |V=2|P|X|  CC   |M|     PT      |       sequence number         |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                           timestamp                           |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |           synchronization source (SSRC) identifier            |
     +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
     |            contributing source (CSRC) identifiers             |
     |                             ....                              |
     +=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+
     |            VP9 payload descriptor (integer #octets)           |
     :                                                               :
     |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                               :                               |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+                               |
     |                                                               |
     +                                                               |
     :                          VP9 payload                          :
     |                                                               |
     |                               +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     |                               :    OPTIONAL RTP padding       |
     +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     */
    //1000
    val v = 2 //2 bits
    val p = 0 //1 bit     IF PADDING
    val cc = 1 // 4 bit Contributors
    val m = 0 //1bit if not temporal



    val versionPX:Byte = 8.toByte()
    val contributorCountM = 2.toByte()
    val payloadType = 0//RFC3550]



}