-- Seoul sigungu regions used by the region selection API.

INSERT INTO regions (sido, sigungu, dong, code, code_type) VALUES
    ('서울특별시', '종로구', '', '11110', 'SIGUNGU'),
    ('서울특별시', '중구', '', '11140', 'SIGUNGU'),
    ('서울특별시', '용산구', '', '11170', 'SIGUNGU'),
    ('서울특별시', '성동구', '', '11200', 'SIGUNGU'),
    ('서울특별시', '광진구', '', '11215', 'SIGUNGU'),
    ('서울특별시', '동대문구', '', '11230', 'SIGUNGU'),
    ('서울특별시', '중랑구', '', '11260', 'SIGUNGU'),
    ('서울특별시', '성북구', '', '11290', 'SIGUNGU'),
    ('서울특별시', '강북구', '', '11305', 'SIGUNGU'),
    ('서울특별시', '도봉구', '', '11320', 'SIGUNGU'),
    ('서울특별시', '노원구', '', '11350', 'SIGUNGU'),
    ('서울특별시', '은평구', '', '11380', 'SIGUNGU'),
    ('서울특별시', '서대문구', '', '11410', 'SIGUNGU'),
    ('서울특별시', '마포구', '', '11440', 'SIGUNGU'),
    ('서울특별시', '양천구', '', '11470', 'SIGUNGU'),
    ('서울특별시', '강서구', '', '11500', 'SIGUNGU'),
    ('서울특별시', '구로구', '', '11530', 'SIGUNGU'),
    ('서울특별시', '금천구', '', '11545', 'SIGUNGU'),
    ('서울특별시', '영등포구', '', '11560', 'SIGUNGU'),
    ('서울특별시', '동작구', '', '11590', 'SIGUNGU'),
    ('서울특별시', '관악구', '', '11620', 'SIGUNGU'),
    ('서울특별시', '서초구', '', '11650', 'SIGUNGU'),
    ('서울특별시', '강남구', '', '11680', 'SIGUNGU'),
    ('서울특별시', '송파구', '', '11710', 'SIGUNGU'),
    ('서울특별시', '강동구', '', '11740', 'SIGUNGU')
ON CONFLICT (code) WHERE code IS NOT NULL DO UPDATE SET
    sido = EXCLUDED.sido,
    sigungu = EXCLUDED.sigungu,
    dong = EXCLUDED.dong,
    code_type = EXCLUDED.code_type;
