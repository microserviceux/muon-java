package io.muoncore.crud;

public class OldMuon /* implements MuonService*/ {

//    private Logger log = Logger.getLogger(OldMuon.class.getName());
//
//    private Discovery discovery;
//
//    private List<MuonTransport> transports = new ArrayList<MuonTransport>();
//    private List<MuonTransport> nonInitTransports = new ArrayList<MuonTransport>();
//
//    private TransportList<MuonResourceTransport> resourceTransports = new TransportList<MuonResourceTransport>();
//    private TransportList<MuonStreamTransport> streamingTransports = new TransportList<MuonStreamTransport>();
//
//    private ServiceSpecification specification = new ServiceSpecification();
//
//    private Codecs codecs = new Codecs();
//
//    private String serviceIdentifier;
//
//    private boolean started = false;
//
//    private List<String> tags = new ArrayList<String>();
//
//    public OldMuon(Discovery discovery) {
//        this.discovery = discovery;
//    }
//
//    public void start() throws URISyntaxException {
//        for(MuonTransport transport: nonInitTransports) {
//            initialiseTransport(transport);
//        }
//        discovery.advertiseLocalService(getCurrentLocalDescriptor());
//
//        addSpecEndpoints();
//        EventLogger.initialise(this);
//
//        started = true;
//    }
//
//    private JsonObject filterOperations(Collection<Operation> elems, Class<?> filter) {
//    	JsonObject response = new JsonObject();
//    	JsonArray arOps = new JsonArray();
//
//    	for(Operation elem : elems) {
//    		if(filter == null || filter.isInstance(elem)) {
//    			JsonObject arElem = new JsonObject();
//    			arElem.addProperty("endpoint", elem.getResource());
//    			arElem.addProperty("method", elem.getClass().getSimpleName().toLowerCase());
//    			// arElem.addProperty("return_type", elem.getType().getSimpleName().toLowerCase());
//    			arOps.add(arElem);
//    		}
//    	}
//    	response.add("operations", arOps);
//
//        response.add("amqp-protocol-version", new JsonPrimitive("5"));
//
//    	return response;
//    }
//
//    private void addSpecEndpoints() {
//    	final Gson gson = new Gson();
//
//    	// Add endpoints for schemas and specification
//    	onQuery("/muon/introspect", Map.class, new MuonQueryListener<Map>() {
//    		@Override
//    		public MuonFuture<Map> onQuery(MuonResourceEvent<Map> queryEvent) {
//    			JsonObject response = filterOperations(specification.getOperations(), null);
//    			return new ImmediateReturnFuture<Map>(gson.fromJson(response, Map.class));
//    		}
//    	});
//
//    	for(final Class<?> t : Operation.availableTypes()) {
//    		String lowT = t.getSimpleName().toLowerCase();
//
//    		onQuery("/muon/introspect/" + lowT, Map.class, new MuonQueryListener<Map>() {
//				@Override
//				public MuonFuture<?> onQuery(MuonResourceEvent<Map> queryEvent) {
//					JsonObject response = filterOperations(specification.getOperations(), t);
//					return new ImmediateReturnFuture<Map>(gson.fromJson(response, Map.class));
//				}
//    		});
//    	}
//	}
//
//	@Override
//    public List<String> getTags() {
//        return tags;
//    }
//
//    @Override
//    public Codecs getCodecs() {
//        return codecs;
//    }
//
//    public ServiceDescriptor getCurrentLocalDescriptor() throws URISyntaxException {
//
//        Set<URI> resourceConnectionUris = new HashSet<URI>();
//
//        for(MuonTransport t: resourceTransports.all()) {
//            resourceConnectionUris.add(t.getLocalConnectionURI());
//        }
//
//        Set<URI> streamConnectionUris = new HashSet<URI>();
//
//        for(MuonTransport t: streamingTransports.all()) {
//            streamConnectionUris.add(t.getLocalConnectionURI());
//        }
//
//        return new ServiceDescriptor(serviceIdentifier,
//                tags,
//                new ArrayList<URI>(resourceConnectionUris),
//                new ArrayList<URI>(streamConnectionUris));
//    }
//
//    @Override
//    public void registerTransport(MuonTransport transport) {
//        transports.add(transport);
//
//        if(transport instanceof MuonResourceTransport) {
//            resourceTransports.addTransport((MuonResourceTransport) transport);
//        }
//        if(transport instanceof MuonStreamTransport) {
//            streamingTransports.addTransport((MuonStreamTransport) transport);
//        }
//
//        if (!started) {
//            nonInitTransports.add(transport);
//        } else {
//            //command start addition of a transport, maybe not ideal, but certainly not prevented either.
//            //permits the addition of new transport dynamically, which may be useful.
//            initialiseTransport(transport);
//        }
//    }
//
//    private void initialiseTransport(MuonTransport transport) {
//        try {
//            transport.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        log.info("Muon: Starting transport " + transport.getClass().getSimpleName());
//    }
//
//    public String getServiceIdentifer() {
//        return serviceIdentifier;
//    }
//
//    public void setServiceIdentifer(String serviceIdentifer) {
//        this.serviceIdentifier = serviceIdentifer;
//    }
//
//    public void addTag(String tag) {
//        this.tags.add(tag);
//    }
//
//    public void addTags(String ... tag) {
//        this.tags.addAll(Arrays.asList(tag));
//    }
//
//    private <T> MuonFuture<MuonClient.MuonResult<T>> dispatchEvent(MuonResourceEvent ev, String resourceQuery, Class<T> type) {
//        MuonResourceTransport trans = transport(ev);
//
//        if (trans == null) {
//            final MuonClient.MuonResult ret = new MuonClient.MuonResult();
//            ret.setEvent(MuonResourceEventBuilder.event(null)
//                    .withUri(resourceQuery)
//                    .withHeader("Status", "404")
//                    .withHeader("message", "No transport can be found to dispatch this message")
//                    .build());
//            ret.setSuccess(false);
//
//            return new ImmediateReturnFuture<MuonClient.MuonResult<T>>(ret);
//        }
//        ev.addHeader("Accept", getAcceptHeader(type));
//        encode(ev, trans.getCodecType());
//
//        MuonClient.MuonResult<T> ret = trans.emitForReturn(resourceQuery, ev);
//        decode(ret.getResponseEvent(), trans.getCodecType(), type);
//
//        //TODO, replace with a streaming promise version pushed all the way down through the transport layers.
//        return new ImmediateReturnFuture<MuonClient.MuonResult<T>>(ret);
//    }
//
//    private String getAcceptHeader(Class type) {
//        Set<String> acceptsContentTypes = codecs.getBinaryContentTypesAvailable(type);
//
//        StringBuilder buf = new StringBuilder();
//        for(String accept: acceptsContentTypes) {
//            buf.append(accept);
//            buf.append(",");
//        }
//
//        return buf.toString().substring(0, buf.length() -1);
//    }
//
//
//    private <T> void encode(MuonResourceEvent<T> ev, TransportCodecType type) {
//        if (ev.getDecodedContent() != null) {
//            if (type == TransportCodecType.BINARY) {
//                byte[] content = codecs.encodeToByte(ev.getDecodedContent());
//                ev.setBinaryEncodedContent(content);
//            } else {
//                String content = codecs.encodeToString(ev.getDecodedContent());
//                ev.setTextEncodedContent(content);
//            }
//        }
//    }
//
//    private <T> void decode(MuonResourceEvent<T> ev, TransportCodecType codecType, Class<T> type) {
//        if (ev == null) return;
//        if (codecType== TransportCodecType.BINARY) {
//            T obj = codecs.decodeObject(ev.getBinaryEncodedContent(),ev.getContentType(), type);
//            ev.setDecodedContent(obj);
//        } else {
//            T obj = codecs.decodeObject(ev.getTextEncodedContent(),ev.getContentType(), type);
//            ev.setDecodedContent(obj);
//        }
//    }
//
//    private <T> void decode(MuonMessageEvent<T> ev, TransportCodecType codecType, Class<T> type) {
//        if (ev == null) return;
//        if (codecType== TransportCodecType.BINARY) {
//            T obj = codecs.decodeObject(ev.getBinaryEncodedContent(), ev.getContentType(), type);
//            ev.setDecodedContent(obj);
//        } else {
//            T obj = codecs.decodeObject(ev.getTextEncodedContent(), ev.getContentType(), type);
//            ev.setDecodedContent(obj);
//        }
//    }
//
//    public <T> MuonFuture<MuonResult<T>> query(String resourceQuery, Class<T> type) {
//        MuonResourceEvent<T> ev = null;
//        try {
//            ev = resourceEvent("get", new MuonResourceEvent<T>(new URI(resourceQuery)));
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
//        return dispatchEvent(ev, resourceQuery, type);
//    }
//
//    public <T> MuonFuture<MuonResult<T>> query(MuonResourceEvent<T> payload, Class<T> type) {
//        MuonResourceEvent<T> ev = resourceEvent("query", payload);
//        return dispatchEvent(ev, payload.getResource(), type);
//    }
//
//    @Override
//    public <T> MuonFuture<MuonResult<T>> command(String resource, MuonResourceEvent<T> payload, Class<T> type) {
//        MuonResourceEvent<T> ev = resourceEvent("command", payload);
//        return dispatchEvent(ev, resource, type);
//    }
//
//    static <T> MuonResourceEvent<T> resourceEvent(String verb, MuonResourceEvent<T> payload) {
//        payload.addHeader("verb", verb);
//        return payload;
//    }
//
//    @Override
//    public <T> void onQuery(String resource, final Class<T> type, final MuonQueryListener<T> listener) {
//    	// Add operation to service specification
//    	specification.addQuery(resource, type, listener);
//
//        //TODO, extract this into some lifecycle init during start.
//        //instead just store this.
//        for(final MuonResourceTransport transport: resourceTransports.all()) {
//            transport.listenOnResource(resource, "query", type, new EventResourceTransportListener<T>() {
//                @Override
//                public MuonFuture onEvent(String name, MuonResourceEvent<T> obj) {
//                    decode(obj, transport.getCodecType(), type);
//                    return listener.onQuery(obj);
//                }
//            });
//        }
//    }
//
//    @Override
//    public <T> void onCommand(String resource, final Class<T> type, final MuonCommandListener<T> listener) {
//    	// Add operation to service specification
//    	specification.addCommand(resource, type, listener);
//
//        //TODO, extract this into some lifecycle init during start.
//        //instead just store this.
//        for(final MuonResourceTransport transport: resourceTransports.all()) {
//            transport.listenOnResource(resource, "command", type, new EventResourceTransportListener<T>() {
//                @Override
//                public MuonFuture onEvent(String name, MuonResourceEvent<T> obj) {
//                    decode(obj, transport.getCodecType(), type);
//                    return listener.onCommand(obj);
//                }
//            });
//        }
//    }
//
//    @Override
//    public void shutdown() {
//        for(MuonTransport transport: transports) {
//            transport.shutdown();
//        }
//        log.info("Muon has shutdown");
//    }
//
//    public List<ServiceDescriptor> discoverServices() {
//        return discovery.getKnownServices();
//    }
//
//    public static interface EventMessageTransportListener<T> {
//        void onEvent(String name, MuonMessageEvent<T> obj);
//    }
//
//    public static interface EventResourceTransportListener<T> {
//        MuonFuture onEvent(String name, MuonResourceEvent<T> obj);
//    }
//
//    MuonResourceTransport transport(MuonResourceEvent event) {
//        ServiceDescriptor remoteDescriptor = discovery.getService(event.getUri());
//        if (remoteDescriptor == null) {
//            return null;
//        }
//        return resourceTransports.findBestResourceTransport(remoteDescriptor);
//    }
//
//    public List<MuonStreamRegister> getStreams() {
//        return null;//streams;
//    }
//
//    /**
//     *
//     * @param streamName
//     */
//    public <T> void streamSource(String streamName, Class<T> type, MuonStreamGenerator<T> generator) {
//    	// Add operation to service specification
//    	specification.addStream(streamName, type, generator);
//
//        for(MuonStreamTransport transport: streamingTransports.all()) {
//            transport.provideStreamSource(streamName, generator);
//        }
//    }
//
//    public <T> void streamSource(String streamName, Class<T> type, Publisher<T> pub) {
//        streamSource(streamName, type, new MuonStreamExistingGenerator<T>(pub));
//    }
//
//    /**
//     * Find a remote stream to subscribe to and subscribe using the given subscriber.
//     *
//     * @param url the Muon url muon://serviceName/streamname
//     * @param subscriber
//     * @throws URISyntaxException
//     */
//    public <T> void subscribe(String url, Class<T> type, Subscriber<T> subscriber) throws URISyntaxException {
//        subscribe(url, type, new HashMap<String, String>(), subscriber);
//    }
//
//    public <T> void subscribe(String url, Class<T> type, Map<String, String> params, Subscriber<T> subscriber) throws URISyntaxException {
//
//        URI uri = new URI(url);
//
//        String host = uri.getHost();
//
//        if (uri.getQuery() != null && uri.getQuery().trim().length() > 0) {
//            params.putAll(Splitter.on('&')
//                    .trimResults()
//                    .withKeyValueSeparator("=")
//                    .split(uri.getQuery()));
//        }
//        ServiceDescriptor descriptor = discovery.getService(uri);
//
//        if (descriptor == null) {
//            subscriber.onError(new IllegalStateException("Service not found"));
//            return;
//        }
//
//        MuonStreamTransport t = streamingTransports.findBestStreamTransport(descriptor);
//
//        if (t == null) {
//            log.warning("Stream subscription to " + url + " cannot be made, no transport can connect using the connection details " + descriptor.getStreamConnectionUrls());
//            subscriber.onError(new IllegalStateException("Cannot see the remote service " + host));
//            return;
//        }
//
//        t.subscribeToStream(url, type, params, subscriber);
//    }
}
