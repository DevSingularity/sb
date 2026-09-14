## Annotations used:

- `@Entity`: marks a java class as a persistent domain object. It tells JPA and Hibernate that this class represents database data. SB performs automatic db operations w/o us writing raw sql. Does Table Mapping, Row-to-Object conversion, Object-to-Row conversion, Schema Generation.
- `@Table(name = "tbl_users)`: Specifies a custom database table name if it differs from the class name.
- `@Id`: Marks a field a primary key of table.
- `@GeneratedValue`: specifies how the pk value is generated. eg. `@GeneratedValue(strategy = GenerationType.IDENTITY)`

## Steps:
1. setup project
2. setup application.yaml
3. setup .env vars
4. start with entity