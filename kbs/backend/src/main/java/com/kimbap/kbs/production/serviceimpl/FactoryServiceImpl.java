@Service
public class FactoryServiceImpl implements FactoryService {

    @Autowired
    private FactoryMapper factoryMapper;

    @Override
    public List<FactoryVO> getFactoryList() {
        return factoryMapper.getFactoryList();
    }

}
