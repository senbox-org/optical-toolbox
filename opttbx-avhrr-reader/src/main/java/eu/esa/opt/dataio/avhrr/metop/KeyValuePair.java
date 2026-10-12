package eu.esa.opt.dataio.avhrr.metop;

class KeyValuePair {

    final String key;
    final String value;

    public KeyValuePair(String field) {
        key = field.substring(0, 30).trim();
        value = field.substring(32).trim();
    }
}
